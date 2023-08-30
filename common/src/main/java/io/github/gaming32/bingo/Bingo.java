package io.github.gaming32.bingo;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.BingoGameMode;
import io.github.gaming32.bingo.network.BingoNetwork;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.fabricmc.api.EnvType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
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

    public static boolean showOtherTeam;

    public static BingoGame activeGame;
    public static final Set<ServerPlayer> needAdvancementsClear = new HashSet<>();

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            final CommandNode<CommandSourceStack> bingoCommand = dispatcher.register(literal("bingo")
                .then(literal("start")
                    .requires(source -> source.hasPermission(2) && activeGame == null)
                    .then(argument("team1", TeamArgument.team())
                        .then(argument("team2", TeamArgument.team())
                            .executes(Bingo::startGame)
                        )
                    )
                )
                .then(literal("stop")
                    .requires(source -> source.hasPermission(2) && activeGame != null)
                    .executes(ctx -> {
                        activeGame.endGame(ctx.getSource().getServer().getPlayerList(), activeGame.getWinner(true));
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(literal("board")
                    .requires(source -> activeGame != null)
                    .executes(ctx -> {
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
                                return Component.translatable("bingo.board.title");
                            }
                        });
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(literal("copy")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> ComponentUtils.wrapInSquareBrackets(
                                Component.translatable("bingo.board.copy")
                            ).withStyle(s -> s
                                .withColor(ChatFormatting.GREEN)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, activeGame.getBoard().toString())) // TODO: I18n?
                            ), false);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            );

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
                )
            );
        });

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (activeGame != null) {
                activeGame.addPlayer(player);
            }
        });

        PlayerEvent.PLAYER_QUIT.register(needAdvancementsClear::remove);

        LifecycleEvent.SERVER_STOPPED.register(instance -> activeGame = null);

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

        if (Platform.getEnv() == EnvType.CLIENT) {
            ClientLifecycleEvent.CLIENT_SETUP.register(mc -> BingoClient.init());
        }

        LOGGER.info("I got the diagonal!");
    }

    private static int startGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final int difficulty = getArg(
            context, "difficulty", () -> 3, IntegerArgumentType::getInteger // TODO: Default to 2 when that works
        );
        final long seed = getArg(
            context, "seed", RandomSupport::generateUniqueSeed, LongArgumentType::getLong
        );
        final PlayerTeam team1 = TeamArgument.getTeam(context, "team1");
        final PlayerTeam team2 = TeamArgument.getTeam(context, "team2");

        if (team1 == team2) {
            // Should probably be a CommandSyntaxException?
            throw new CommandRuntimeException(Component.translatable("bingo.duplicate_teams"));
        }
        final MinecraftServer server = context.getSource().getServer();
        final PlayerList playerList = server.getPlayerList();

        final BingoBoard board;
        try {
            board = BingoBoard.generate(difficulty, RandomSource.create(seed), server.getLootData());
        } catch (Exception e) {
            LOGGER.error("Error generating bingo board", e);
            throw new CommandRuntimeException(Component.translatable(
                e instanceof JsonSyntaxException ? "bingo.start.invalid_goal" : "bingo.start.failed"
            ));
        }
        LOGGER.info("Generated board (seed {}):\n{}", seed, board);

        activeGame = new BingoGame(board, BingoGameMode.STANDARD, team1, team2); // TODO: Implement gamemode choosing
        updateCommandTree(playerList);
        playerList.getPlayers().forEach(activeGame::addPlayer);
        playerList.broadcastSystemMessage(Component.translatable(
            "bingo.started", Component.translatable("bingo.difficulty." + difficulty)
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
            for (final ParsedCommandNode<?> node : check.getNodes()) {
                if (node.getNode() instanceof ArgumentCommandNode<?, ?> argNode) {
                    if (argNode.getName().equals(arg)) {
                        return argGetter.apply(check, arg);
                    }
                }
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
}
