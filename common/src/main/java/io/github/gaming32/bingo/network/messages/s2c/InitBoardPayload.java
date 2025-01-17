package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.mode.BingoGameMode;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.github.gaming32.bingo.util.BingoStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record InitBoardPayload(
    int size,
    ActiveGoal[] goals,
    BingoBoard.Teams[] states,
    String[] teams,
    BingoGameMode.RenderMode renderMode
) implements AbstractCustomPayload {
    public static final Type<InitBoardPayload> TYPE = AbstractCustomPayload.type("init_board");
    public static final StreamCodec<RegistryFriendlyByteBuf, InitBoardPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, InitBoardPayload::size,
        ActiveGoal.STREAM_CODEC.apply(BingoStreamCodecs.array(ActiveGoal[]::new)), InitBoardPayload::goals,
        BingoBoard.Teams.STREAM_CODEC.apply(BingoStreamCodecs.array(BingoBoard.Teams[]::new)), InitBoardPayload::states,
        ByteBufCodecs.STRING_UTF8.apply(BingoStreamCodecs.array(String[]::new)), InitBoardPayload::teams,
        BingoGameMode.RenderMode.STREAM_CODEC, InitBoardPayload::renderMode,
        InitBoardPayload::new
    );

    public static InitBoardPayload create(BingoGame game, BingoBoard.Teams[] states) {
        final BingoBoard board = game.getBoard();

        return new InitBoardPayload(
            board.getSize(),
            board.getGoals(),
            states,
            Arrays.stream(game.getTeams())
                .map(PlayerTeam::getName)
                .toArray(String[]::new),
            game.getGameMode().getRenderMode()
        );
    }

    @NotNull
    @Override
    public Type<InitBoardPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        ClientPayloadHandler.get().handleInitBoard(this, context.level());
    }
}
