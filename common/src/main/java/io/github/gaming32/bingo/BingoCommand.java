package io.github.gaming32.bingo;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.ext.CommandContextExt;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.BingoGameMode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BingoCommand {
    public static final SuggestionProvider<CommandSourceStack> ACTIVE_GOAL_SUGGESTOR = (context, builder) -> {
        if (Bingo.activeGame == null) {
            return builder.buildFuture();
        }
        return SharedSuggestionProvider.suggestResource(
            Arrays.stream(Bingo.activeGame.getBoard().getGoals())
                .map(ActiveGoal::getGoal)
                .map(BingoGoal::getId),
            builder
        );
    };

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext registry,
        Commands.CommandSelection selection
    ) {
        final CommandNode<CommandSourceStack> bingoCommand = dispatcher.register(literal("bingo")
//            .then(literal("test")
//                .executes(ctx -> {
//                    final BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
//                    ctx.getSource().getLevel()
//                        .structureManager()
//                        .getStructureWithPieceAt(pos, StructureTags.VILLAGE)
//                        .getPieces()
//                        .stream()
//                        .filter(p -> p.getBoundingBox().isInside(pos))
//                        .filter(p -> p.getType() == StructurePieceType.JIGSAW)
//                        .map(p -> (PoolElementStructurePiece)p)
//                        .forEach(System.out::println);
//                    System.out.println();
//                    return 1;
//                })
//            )
            .then(literal("start")
                .requires(source -> source.hasPermission(2))
            )
            .then(literal("stop")
                .requires(source -> source.hasPermission(2) && Bingo.activeGame != null)
                .executes(ctx -> {
                    if (Bingo.activeGame == null) {
                        throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                    }
                    Bingo.activeGame.endGame(ctx.getSource().getServer().getPlayerList(), Bingo.activeGame.getWinner(true));
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(literal("board")
                .requires(source -> Bingo.activeGame != null)
                .executes(ctx -> {
                    if (Bingo.activeGame == null) {
                        throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                    }
                    int size = Bingo.activeGame.getBoard().getSize();

                    MenuType<?> menuType = switch (size) {
                        case 1 -> MenuType.GENERIC_9x1;
                        case 2 -> MenuType.GENERIC_9x2;
                        case 3 -> MenuType.GENERIC_9x3;
                        case 4 -> MenuType.GENERIC_9x4;
                        case 5 -> MenuType.GENERIC_9x5;
                        case 6 -> MenuType.GENERIC_9x6;
                        default -> throw new CommandRuntimeException(Bingo.translatable("bingo.cannot_show_board", size));
                    };

                    ctx.getSource().getPlayerOrException().openMenu(new MenuProvider() {
                        @Override
                        public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                            final var menu = new ChestMenu(menuType, syncId, inventory, new SimpleContainer(9 * size), size) {
                                @Override
                                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                                    sendAllDataToRemote(); // Same as in spectator mode
                                }
                            };
                            int minX = (9 - size) / 2;
                            for (int x = 0; x < size; x++) {
                                for (int y = 0; y < size; y++) {
                                    menu.getContainer().setItem(minX + y * 9 + x, Bingo.activeGame.getBoard().getGoal(x, y).toSingleStack());
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
                        if (Bingo.activeGame == null) {
                            throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                        }
                        ctx.getSource().sendSuccess(() -> ComponentUtils.wrapInSquareBrackets(
                            Bingo.translatable("bingo.board.copy")
                        ).withStyle(s -> s
                            .withColor(ChatFormatting.GREEN)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Bingo.translatable("chat.copy.click")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Bingo.activeGame.getBoard().toString())) // TODO: I18n?
                        ), false);
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(literal("difficulties")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> {
                        if (Bingo.activeGame == null) {
                            throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                        }
                        final BingoBoard board = Bingo.activeGame.getBoard();
                        final StringBuilder line = new StringBuilder(board.getSize());
                        for (int y = 0; y < board.getSize(); y++) {
                            for (int x = 0; x < board.getSize(); x++) {
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
                .requires(source -> source.hasPermission(2) && Bingo.activeGame != null)
                .then(argument("players", EntityArgument.players())
                    .then(literal("award")
                        .then(argument("goal", ResourceLocationArgument.id())
                            .suggests(ACTIVE_GOAL_SUGGESTOR)
                            .executes(context -> {
                                if (Bingo.activeGame == null) {
                                    throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                                }
                                final Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                                final ResourceLocation goalId = ResourceLocationArgument.getId(context, "goal");

                                int success = 0;
                                for (final ActiveGoal goal : Bingo.activeGame.getBoard().getGoals()) {
                                    if (goal.getGoal().getId().equals(goalId)) {
                                        for (final ServerPlayer player : players) {
                                            if (Bingo.activeGame.award(player, goal)) {
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
                                if (Bingo.activeGame == null) {
                                    throw new CommandRuntimeException(Component.translatable("bingo.no_game_running"));
                                }
                                final Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                                final ResourceLocation goalId = ResourceLocationArgument.getId(context, "goal");

                                int success = 0;
                                for (final ActiveGoal goal : Bingo.activeGame.getBoard().getGoals()) {
                                    if (goal.getGoal().getId().equals(goalId)) {
                                        for (final ServerPlayer player : players) {
                                            if (Bingo.activeGame.revoke(player, goal)) {
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
                    .then(literal("--size")
                        .then(argument("size", IntegerArgumentType.integer(BingoBoard.MIN_SIZE, BingoBoard.MAX_SIZE))
                            .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                        )
                    )
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
                    .then(literal("--gamemode")
                        .then(argument("gamemode", StringArgumentType.word())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                BingoGameMode.GAME_MODES.keySet(), builder
                            ))
                            .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                        )
                    )
                    .then(literal("--require-client")
                        .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                    )
                )
            );
            CommandNode<CommandSourceStack> currentCommand = startCommand;
            for (int i = 1; i <= 32; i++) {
                final CommandNode<CommandSourceStack> subCommand = argument("team" + i, TeamArgument.team())
                    .executes(BingoCommand::startGame)
                    .build();
                currentCommand.addChild(subCommand);
                currentCommand = subCommand;
            }
        }
    }

    private static int startGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (Bingo.activeGame != null) {
            Bingo.activeGame.endGame(context.getSource().getServer().getPlayerList(), Bingo.activeGame.getWinner(true));
        }

        final int difficulty = getArg(context, "difficulty", () -> 2, IntegerArgumentType::getInteger);
        final long seed = getArg(context, "seed", RandomSupport::generateUniqueSeed, LongArgumentType::getLong);
        final ResourceLocation requiredGoalId = getArg(context, "required_goal", () -> null, ResourceLocationArgument::getId);
        final int size = getArg(context, "size", () -> BingoBoard.DEFAULT_SIZE, IntegerArgumentType::getInteger);
        final String gamemodeId = getArg(context, "gamemode", () -> "standard", StringArgumentType::getString);
        final boolean requireClient = hasNode(context, "--require-client");

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

        final BingoGameMode gamemode = BingoGameMode.GAME_MODES.get(gamemodeId);
        if (gamemode == null) {
            throw new CommandRuntimeException(Bingo.translatable("bingo.unknown_gamemode", gamemodeId));
        }

        final Component configError = gamemode.checkAllowedConfig(
            new BingoGameMode.GameConfig(gamemode, size, teams)
        );
        if (configError != null) {
            throw new CommandRuntimeException(configError);
        }

        final MinecraftServer server = context.getSource().getServer();
        final PlayerList playerList = server.getPlayerList();

        final BingoBoard board;
        try {
            board = BingoBoard.generate(
                size,
                difficulty,
                teams.size(),
                RandomSource.create(seed),
                server.getLootData(),
                gamemode::isGoalAllowed,
                requiredGoal,
                requireClient
            );
        } catch (Exception e) {
            Bingo.LOGGER.error("Error generating bingo board", e);
            throw new CommandRuntimeException(Bingo.translatable(
                e instanceof JsonSyntaxException ? "bingo.start.invalid_goal" : "bingo.start.failed"
            ).withStyle(s -> s.withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Component.literal(e.getMessage())
            ))));
        }
        Bingo.LOGGER.info("Generated board (seed {}):\n{}", seed, board);

        Bingo.activeGame = new BingoGame(board, gamemode, requireClient, teams.toArray(PlayerTeam[]::new));
        Bingo.updateCommandTree(playerList);
        playerList.getPlayers().forEach(Bingo.activeGame::addPlayer);
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
            if (check.getSource() instanceof CommandSourceStackExt ext) {
                for (final CommandContext<CommandSourceStack> extra : ext.bingo$getExtraContexts()) {
                    if (visited.add(extra)) {
                        toVisit.add(extra);
                    }
                }
            }
        }

        return defaultValue.get();
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

    public static boolean hasNode(CommandContext<?> context, String name) {
        final Set<CommandContext<?>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final Queue<CommandContext<?>> toVisit = new ArrayDeque<>();
        toVisit.add(context);

        while (!toVisit.isEmpty()) {
            final CommandContext<?> check = toVisit.remove();
            for (final ParsedCommandNode<?> node : check.getNodes()) {
                if (node.getNode().getName().equals(name)) {
                    return true;
                }
            }
            if (check.getSource() instanceof CommandSourceStackExt ext) {
                for (final CommandContext<CommandSourceStack> extra : ext.bingo$getExtraContexts()) {
                    if (visited.add(extra)) {
                        toVisit.add(extra);
                    }
                }
            }
        }

        return false;
    }
}
