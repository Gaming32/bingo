package io.github.gaming32.bingo;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import io.github.gaming32.bingo.board.BingoBoard;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Bingo {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static BingoBoard serverBoard, clientBoard;

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            final CommandNode<CommandSourceStack> bingoCommand = dispatcher.register(literal("bingo")
                .requires(ctx -> ctx.hasPermission(2))
                .then(literal("start")
                    .requires(ctx -> serverBoard == null)
                    .executes(ctx -> {
                        final int difficulty = getArg(
                            ctx, "difficulty", () -> 0, IntegerArgumentType::getInteger // TODO: Change default to medium (2)
                        );
                        final long seed = getArg(
                            ctx, "seed", RandomSupport::generateUniqueSeed, LongArgumentType::getLong
                        );
                        final BingoBoard board;
                        try {
                            board = BingoBoard.generate(
                                difficulty, RandomSource.create(seed), ctx.getSource().getServer().getLootData()
                            );
                        } catch (Exception e) {
                            LOGGER.error("Error generating bingo board", e);
                            throw new CommandRuntimeException(Component.translatable("bingo.start.failed"));
                        }
                        LOGGER.info("Generated board:\n{}", board);
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
                            .executes(startCommand.getCommand())
                        )
                    )
                    .then(literal("--seed")
                        .then(argument("seed", LongArgumentType.longArg())
                            .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                            .executes(startCommand.getCommand())
                        )
                    )
                )
            );
        });

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
}
