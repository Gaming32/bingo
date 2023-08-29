package io.github.gaming32.bingo;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.BingoGameMode;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Bingo {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static BingoGame activeGame;
    public static BingoBoard clientBoard;

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            final CommandNode<CommandSourceStack> bingoCommand = dispatcher.register(literal("bingo")
                .requires(source -> source.hasPermission(2))
                .then(literal("start")
                    .requires(ctx -> activeGame == null)
                    .then(argument("team1", TeamArgument.team())
                        .then(argument("team2", TeamArgument.team())
                            .executes(Bingo::startGame)
                        )
                    )
                )
                .then(literal("stop")
                    .requires(ctx -> activeGame != null)
                    .executes(ctx -> {
                        activeGame.endGame(ctx.getSource().getServer().getPlayerList(), activeGame.getWinner(true));
                        return Command.SINGLE_SUCCESS;
                    })
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
            throw new CommandRuntimeException(Component.translatable("bingo.start.failed"));
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
        final Commands commands = playerList.getServer().getCommands();
        final CommandNode<CommandSourceStack> bingoCommand = commands
            .getDispatcher()
            .getRoot()
            .getChild("bingo");
        for (final ServerPlayer player : playerList.getPlayers()) {
            if (bingoCommand.canUse(player.createCommandSourceStack())) {
                commands.sendCommands(player);
            }
        }
    }
}
