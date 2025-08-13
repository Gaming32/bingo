package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BoardShape;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.github.gaming32.bingo.network.messages.both.ManualHighlightPayload;
import io.github.gaming32.bingo.network.messages.s2c.InitBoardPayload;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPayload;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateProgressPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateStatePayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;

public class ClientPayloadHandlerImpl implements ClientPayloadHandler {
    @Override
    public void handleInitBoard(InitBoardPayload payload, Level level) {
        final BoardShape shape = payload.shape();
        final int size = payload.size();
        final int goalCount = shape.getGoalCount(size);
        final String[] teams = payload.teams();

        final Scoreboard scoreboard = level.getScoreboard();
        final PlayerTeam[] playerTeams = new PlayerTeam[teams.length];
        for (int i = 0; i < teams.length; i++) {
            final PlayerTeam team = scoreboard.getPlayerTeam(teams[i]);
            if (team == null) {
                Bingo.LOGGER.error("Unknown team {}", teams[i]);
                return;
            }
            playerTeams[i] = team;
        }

        BingoClient.clientGame = new ClientGame(
            shape,
            size,
            payload.states(),
            payload.goals(),
            playerTeams,
            payload.nerfedTeams(),
            payload.renderMode(),
            new GoalProgress[goalCount],
            Arrays.stream(payload.manualHighlights()).mapToObj(i -> i == 0 ? null : i - 1).toArray(Integer[]::new),
            new MutableInt(payload.manualHighlightModCount())
        );
    }

    @Override
    public void handleRemoveBoard() {
        BingoClient.clientGame = null;
    }

    @Override
    public void handleResyncStates(ResyncStatesPayload payload) {
        if (!checkGamePresent(payload)) return;
        System.arraycopy(
            payload.states(), 0,
            BingoClient.clientGame.states(), 0,
            BingoClient.clientGame.shape().getGoalCount(BingoClient.clientGame.size())
        );
    }

    @Override
    public void handleSyncTeam(SyncTeamPayload payload) {
        BingoClient.clientTeam = BingoClient.receivedClientTeam = payload.team();
    }

    @Override
    public void handleUpdateProgress(UpdateProgressPayload payload) {
        if (!checkGamePresent(payload)) return;
        BingoClient.clientGame.progress()[payload.index()] = new GoalProgress(payload.progress(), payload.maxProgress());
    }

    @Override
    public void handleUpdateState(UpdateStatePayload payload) {
        if (!checkGamePresent(payload)) return;
        final int index = payload.index();
        if (index < 0 || index >= BingoClient.clientGame.shape().getGoalCount(BingoClient.clientGame.size())) {
            Bingo.LOGGER.warn("Invalid {} payload: invalid board index {}", UpdateStatePayload.TYPE, index);
            return;
        }
        BingoClient.clientGame.states()[index] = payload.newState();
    }

    @Override
    public void handleManualHighlight(ManualHighlightPayload payload) {
        if (!checkGamePresent(payload)) return;
        if (payload.slot() < 0 || payload.slot() >= BingoClient.clientGame.shape().getGoalCount(BingoClient.clientGame.size())) return;
        if (payload.value() < 0 || payload.value() > BingoBoard.NUM_MANUAL_HIGHLIGHT_COLORS) return;
        BingoClient.clientGame.manualHighlights()[payload.slot()] = payload.value() == 0 ? null : payload.value() - 1;
        BingoClient.clientGame.manualHighlightModCount().setValue(payload.modCount());
    }

    private static boolean checkGamePresent(CustomPacketPayload payload) {
        if (BingoClient.clientGame == null) {
            Bingo.LOGGER.warn("BingoClient.clientGame == null while handling {}!", payload.type());
            return false;
        }
        return true;
    }
}
