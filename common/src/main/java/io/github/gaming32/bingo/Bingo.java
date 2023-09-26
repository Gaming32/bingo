package io.github.gaming32.bingo;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.logging.LogUtils;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.utils.Env;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.conditions.BingoConditions;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.icons.GoalIconType;
import io.github.gaming32.bingo.data.subs.BingoSubType;
import io.github.gaming32.bingo.ext.CommandContextExt;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.BingoGameMode;
import io.github.gaming32.bingo.network.BingoNetwork;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Bingo {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final RegistrarManager REGISTRAR_MANAGER = RegistrarManager.get(MOD_ID);

    public static boolean showOtherTeam;
    public static BingoGameMode gameMode = BingoGameMode.STANDARD;

    public static BingoGame activeGame;
    public static final Set<ServerPlayer> needAdvancementsClear = new HashSet<>();

    public static final SuggestionProvider<CommandSourceStack> ACTIVE_GOAL_SUGGESTOR = (context, builder) -> {
        if (activeGame == null) {
            return builder.buildFuture();
        }
        return SharedSuggestionProvider.suggestResource(
            Arrays.stream(activeGame.getBoard().getGoals())
                .map(ActiveGoal::getGoal)
                .map(BingoGoal::getId),
            builder
        );
    };

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            final CommandNode<CommandSourceStack> bingoCommand = dispatcher.register(literal("bingo")
                .then(literal("start")
                    .requires(source -> source.hasPermission(2) && activeGame == null)
                )
                .then(literal("stop")
                    .requires(source -> source.hasPermission(2) && activeGame != null)
                    .executes(ctx -> {
                        if (activeGame == null) {
                            throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                        }
                        activeGame.endGame(ctx.getSource().getServer().getPlayerList(), activeGame.getWinner(true));
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(literal("board")
                    .requires(source -> activeGame != null)
                    .executes(ctx -> {
                        if (activeGame == null) {
                            throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                        }
                        ctx.getSource().getPlayerOrException().openMenu(new MenuProvider() {
                            @Override
                            public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                                final var menu = new ChestMenu(MenuType.GENERIC_9x5, syncId, inventory, new SimpleContainer(9 * 5), 5) {
                                    @Override
                                    public void clicked(int slotId, int button, ClickType clickType, Player player) {
                                        sendAllDataToRemote(); // Same as in spectator mode
                                    }
                                };
                                for (int x = 0; x < BingoBoard.SIZE; x++) {
                                    for (int y = 0; y < BingoBoard.SIZE; y++) {
                                        menu.getContainer().setItem(2 + y * 9 + x, activeGame.getBoard().getGoal(x, y).toSingleStack());
                                    }
                                }
                                return menu;
                            }

                            @NotNull
                            @Override
                            public Component getDisplayName() {
                                return Bingo.translatable("bingo.board.title");
                            }
                        });
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(literal("copy")
                        .executes(ctx -> {
                            if (activeGame == null) {
                                throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                            }
                            ctx.getSource().sendSuccess(() -> ComponentUtils.wrapInSquareBrackets(
                                Bingo.translatable("bingo.board.copy")
                            ).withStyle(s -> s
                                .withColor(ChatFormatting.GREEN)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Bingo.translatable("chat.copy.click")))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, activeGame.getBoard().toString())) // TODO: I18n?
                            ), false);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                    .then(literal("difficulties")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> {
                            if (activeGame == null) {
                                throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                            }
                            final BingoBoard board = activeGame.getBoard();
                            final StringBuilder line = new StringBuilder(BingoBoard.SIZE);
                            for (int y = 0; y < BingoBoard.SIZE; y++) {
                                for (int x = 0; x < BingoBoard.SIZE; x++) {
                                    line.append(board.getGoal(x, y).getGoal().getDifficulty());
                                }
                                ctx.getSource().sendSuccess(() -> Component.literal(line.toString()), false);
                                line.setLength(0);
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .then(literal("goals")
                    .requires(source -> source.hasPermission(2) && activeGame != null)
                    .then(argument("players", EntityArgument.players())
                        .then(literal("award")
                            .then(argument("goal", ResourceLocationArgument.id())
                                .suggests(ACTIVE_GOAL_SUGGESTOR)
                                .executes(context -> {
                                    if (activeGame == null) {
                                        throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                                    }
                                    final Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                                    final ResourceLocation goalId = ResourceLocationArgument.getId(context, "goal");

                                    int success = 0;
                                    for (final ActiveGoal goal : activeGame.getBoard().getGoals()) {
                                        if (goal.getGoal().getId().equals(goalId)) {
                                            for (final ServerPlayer player : players) {
                                                if (activeGame.award(player, goal)) {
                                                    success++;
                                                }
                                            }
                                        }
                                    }

                                    final int fSuccess = success;
                                    context.getSource().sendSuccess(() -> Bingo.translatable(
                                        "bingo.award.success", players.size(), fSuccess
                                    ), true);
                                    return success;
                                })
                            )
                        )
                        .then(literal("revoke")
                            .then(argument("goal", ResourceLocationArgument.id())
                                .suggests(ACTIVE_GOAL_SUGGESTOR)
                                .executes(context -> {
                                    if (activeGame == null) {
                                        throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                                    }
                                    final Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                                    final ResourceLocation goalId = ResourceLocationArgument.getId(context, "goal");

                                    int success = 0;
                                    for (final ActiveGoal goal : activeGame.getBoard().getGoals()) {
                                        if (goal.getGoal().getId().equals(goalId)) {
                                            for (final ServerPlayer player : players) {
                                                if (activeGame.revoke(player, goal)) {
                                                    success++;
                                                }
                                            }
                                        }
                                    }

                                    final int fSuccess = success;
                                    context.getSource().sendSuccess(() -> Bingo.translatable(
                                        "bingo.revoke.success", fSuccess, players.size()
                                    ), true);
                                    return success;
                                })
                            )
                        )
                    )
                )
            );

            {
                final CommandNode<CommandSourceStack> startCommand = bingoCommand.getChild("start");
                dispatcher.register(literal("bingo")
                    .then(literal("start")
                        .then(literal("--difficulty")
                            .then(argument("difficulty", IntegerArgumentType.integer(0, 4))
                                .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                            )
                        )
                        .then(literal("--seed")
                            .then(argument("seed", LongArgumentType.longArg())
                                .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                            )
                        )
                        .then(literal("--require-goal")
                            .then(argument("required_goal", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                    BingoGoal.getGoalIds(), builder
                                ))
                                .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                            )
                        )
                    )
                );
                CommandNode<CommandSourceStack> currentCommand = startCommand;
                for (int i = 1; i <= 32; i++) {
                    final CommandNode<CommandSourceStack> subCommand = argument("team" + i, TeamArgument.team())
                        .executes(Bingo::startGame)
                        .build();
                    currentCommand.addChild(subCommand);
                    currentCommand = subCommand;
                }
            }
        });

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (activeGame != null) {
                activeGame.addPlayer(player);
            }
        });

        PlayerEvent.PLAYER_QUIT.register(needAdvancementsClear::remove);

        LifecycleEvent.SERVER_STOPPED.register(instance -> activeGame = null);

        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                BingoTriggers.TRY_USE_ITEM.trigger(serverPlayer, hand);
            }
            return CompoundEventResult.pass();
        });

        BingoConditions.load();
        GoalIconType.load();
        BingoSubType.load();
        BingoTriggers.load();

        ReloadListenerRegistry.register(
            PackType.SERVER_DATA,
            new BingoTag.ReloadListener(),
            BingoTag.ReloadListener.ID
        );
        ReloadListenerRegistry.register(
            PackType.SERVER_DATA,
            new BingoGoal.ReloadListener(),
            BingoGoal.ReloadListener.ID,
            List.of(BingoTag.ReloadListener.ID)
        );

        BingoNetwork.load();

        if (Platform.getEnvironment() == Env.CLIENT) {
            ClientLifecycleEvent.CLIENT_SETUP.register(mc -> BingoClient.init());
        }

        LOGGER.info("I got the diagonal!");
    }

    private static int startGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (activeGame != null) {
            throw new CommandRuntimeException(Component.translatable("bingo.game_running"));
        }

        final int difficulty = getArg(context, "difficulty", () -> 2, IntegerArgumentType::getInteger);
        final long seed = getArg(context, "seed", RandomSupport::generateUniqueSeed, LongArgumentType::getLong);
        final ResourceLocation requiredGoalId = getArg(context, "required_goal", () -> null, ResourceLocationArgument::getId);

        final Set<PlayerTeam> teams = new LinkedHashSet<>();
        for (int i = 1; i <= 32; i++) {
            if (!hasArg(context, "team" + i)) break;
            if (!teams.add(TeamArgument.getTeam(context, "team" + i))) {
                // Should probably be a CommandSyntaxException?
                throw new CommandRuntimeException(Bingo.translatable("bingo.duplicate_teams"));
            }
        }

        final BingoGoal requiredGoal;
        if (requiredGoalId != null) {
            requiredGoal = BingoGoal.getGoal(requiredGoalId);
            if (requiredGoal == null) {
                throw new CommandRuntimeException(Bingo.translatable("bingo.unknown_goal", requiredGoalId));
            }
        } else {
            requiredGoal = null;
        }

        final MinecraftServer server = context.getSource().getServer();
        final PlayerList playerList = server.getPlayerList();

        final BingoBoard board;
        try {
            board = BingoBoard.generate(
                difficulty,
                teams.size(),
                RandomSource.create(seed),
                server.getLootData(),
                gameMode::isGoalAllowed,
                requiredGoal
            );
        } catch (Exception e) {
            LOGGER.error("Error generating bingo board", e);
            throw new CommandRuntimeException(Bingo.translatable(
                e instanceof JsonSyntaxException ? "bingo.start.invalid_goal" : "bingo.start.failed"
            ).withStyle(s -> s.withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Component.literal(e.getMessage())
            ))));
        }
        LOGGER.info("Generated board (seed {}):\n{}", seed, board);

        activeGame = new BingoGame(board, gameMode, teams.toArray(PlayerTeam[]::new)); // TODO: Implement gamemode choosing
        updateCommandTree(playerList);
        playerList.getPlayers().forEach(activeGame::addPlayer);
        playerList.broadcastSystemMessage(Bingo.translatable(
            "bingo.started", Bingo.translatable("bingo.difficulty." + difficulty)
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static <T> T getArg(
        CommandContext<CommandSourceStack> context,
        String arg, Supplier<T> defaultValue,
        BiFunction<CommandContext<CommandSourceStack>, String, T> argGetter
    ) {
        final Set<CommandContext<?>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final Queue<CommandContext<CommandSourceStack>> toVisit = new ArrayDeque<>();
        toVisit.add(context);

        while (!toVisit.isEmpty()) {
            final CommandContext<CommandSourceStack> check = toVisit.remove();
            if (hasArg(check, arg)) {
                return argGetter.apply(check, arg);
            }
            if (context.getSource() instanceof CommandSourceStackExt ext) {
                for (final CommandContext<CommandSourceStack> extra : ext.bingo$getExtraContexts()) {
                    if (visited.add(extra)) {
                        toVisit.add(extra);
                    }
                }
            }
        }

        return defaultValue.get();
    }

    public static void updateCommandTree(PlayerList playerList) {
        playerList.getPlayers().forEach(playerList.getServer().getCommands()::sendCommands);
    }

    public static boolean hasArg(CommandContext<?> context, String name) {
        if (context instanceof CommandContextExt ext) { // false on Forge
            return ext.bingo$hasArg(name);
        }
        for (final ParsedCommandNode<?> node : context.getNodes()) {
            if (node.getNode() instanceof ArgumentCommandNode<?, ?> argument && argument.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static MutableComponent translatable(@Translatable String key, Object... args) {
        return ensureHasFallback(Component.translatable(key, args));
    }

    public static MutableComponent ensureHasFallback(MutableComponent component) {
        if (component.getContents() instanceof TranslatableContents translatable && translatable.getFallback() == null) {
            final MutableComponent result = Component.translatableWithFallback(
                translatable.getKey(), component.getString(), translatable.getArgs()
            ).setStyle(component.getStyle());
            result.getSiblings().addAll(component.getSiblings());
            return result;
        }
        return component;
    }
}
