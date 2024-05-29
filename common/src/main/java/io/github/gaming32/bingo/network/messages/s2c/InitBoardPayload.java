package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.BingoGameMode;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientGoal;
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
    ClientGoal[] goals,
    BingoBoard.Teams[] states,
    String[] teams,
    BingoGameMode.RenderMode renderMode
) implements AbstractCustomPayload {
    public static final Type<InitBoardPayload> TYPE = AbstractCustomPayload.type("init_board");
    public static final StreamCodec<RegistryFriendlyByteBuf, InitBoardPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, p -> p.size,
        ClientGoal.STREAM_CODEC.apply(BingoStreamCodecs.array(ClientGoal[]::new)), p -> p.goals,
        BingoBoard.Teams.STREAM_CODEC.apply(BingoStreamCodecs.array(BingoBoard.Teams[]::new)), p -> p.states,
        ByteBufCodecs.STRING_UTF8.apply(BingoStreamCodecs.array(String[]::new)), p -> p.teams,
        BingoGameMode.RenderMode.STREAM_CODEC, p -> p.renderMode,
        InitBoardPayload::new
    );

    public static InitBoardPayload create(BingoGame game, BingoBoard.Teams[] states) {
        final BingoBoard board = game.getBoard();

        final int size = board.getSize();
        final ClientGoal[] goals = new ClientGoal[board.getGoals().length];

        for (int i = 0; i < goals.length; i++) {
            goals[i] = new ClientGoal(board.getGoals()[i]);
        }

        final String[] teams = Arrays.stream(game.getTeams()).map(PlayerTeam::getName).toArray(String[]::new);
        final var renderMode = game.getGameMode().getRenderMode();

        return new InitBoardPayload(size, goals, states, teams, renderMode);
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
