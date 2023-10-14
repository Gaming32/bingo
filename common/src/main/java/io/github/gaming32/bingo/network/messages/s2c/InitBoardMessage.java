package io.github.gaming32.bingo.network.messages.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.ClientGame;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.BingoGameMode;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.ClientGoal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.Arrays;

public class InitBoardMessage extends BaseS2CMessage {
    private final int size;
    private final ClientGoal[] goals;
    private final BingoBoard.Teams[] states;
    private final String[] teams;
    private final BingoGameMode.RenderMode renderMode;

    public InitBoardMessage(BingoGame game, BingoBoard.Teams[] states) {
        final BingoBoard board = game.getBoard();

        this.size = board.getSize();
        this.goals = new ClientGoal[board.getGoals().length];
        this.states = states; // Obfuscated states

        for (int i = 0; i < goals.length; i++) {
            this.goals[i] = new ClientGoal(board.getGoals()[i]);
        }

        this.teams = Arrays.stream(game.getTeams()).map(PlayerTeam::getName).toArray(String[]::new);
        this.renderMode = game.getGameMode().getRenderMode();
    }

    public InitBoardMessage(FriendlyByteBuf buf) {
        size = buf.readVarInt();
        goals = buf.readList(ClientGoal::new).toArray(ClientGoal[]::new);
        states = buf.readList(b -> BingoBoard.Teams.fromBits(b.readVarInt())).toArray(BingoBoard.Teams[]::new);
        teams = buf.readList(FriendlyByteBuf::readUtf).toArray(String[]::new);
        renderMode = buf.readEnum(BingoGameMode.RenderMode.class);
    }

    @Override
    public MessageType getType() {
        return BingoS2C.INIT_BOARD;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(size);
        buf.writeCollection(Arrays.asList(goals), (b, v) -> v.serialize(b));
        buf.writeCollection(Arrays.asList(states), (b, v) -> b.writeVarInt(v.toBits()));
        buf.writeCollection(Arrays.asList(teams), FriendlyByteBuf::writeUtf);
        buf.writeEnum(renderMode);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        final Scoreboard scoreboard = context.getPlayer().getScoreboard();
        final PlayerTeam[] playerTeams = new PlayerTeam[teams.length];
        for (int i = 0; i < teams.length; i++) {
            final PlayerTeam team = scoreboard.getPlayerTeam(teams[i]);
            if (team == null) {
                Bingo.LOGGER.error("Unknown team " + teams[i]);
                return;
            }
            playerTeams[i] = team;
        }

        BingoClient.clientGame = new ClientGame(
            size, states, goals, playerTeams, renderMode, new GoalProgress[size * size]
        );
    }
}
