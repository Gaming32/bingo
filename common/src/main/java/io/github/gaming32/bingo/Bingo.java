package io.github.gaming32.bingo;

import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import io.github.gaming32.bingo.board.BingoBoard;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class Bingo {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static BingoBoard serverBoard, clientBoard;

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(literal("bingo")
                .requires(ctx -> ctx.hasPermission(2))
                .then(literal("start")
                    .requires(ctx -> serverBoard == null)
                    .executes(ctx -> {
                        return 0;
                    })
                )
            );
        });

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

        LifecycleEvent.SERVER_STARTED.register(server ->
            LOGGER.info("Generated board:\n{}", BingoBoard.generate(0, RandomSource.create(), server.getLootData()))
        );

        LOGGER.info("I got the diagonal!");
    }
}
