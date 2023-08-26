package io.github.gaming32.bingo;

import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import io.github.gaming32.bingo.board.BingoBoard;
import org.slf4j.Logger;

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
        LOGGER.info("I got the diagonal!");
    }
}
