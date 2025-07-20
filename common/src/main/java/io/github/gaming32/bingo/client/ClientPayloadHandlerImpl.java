package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.github.gaming32.bingo.network.messages.s2c.InitBoardPayload;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPayload;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateEndTimePayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateProgressPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateStatePayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class ClientPayloadHandlerImpl implements ClientPayloadHandler {
    @Override
    public void handleInitBoard(InitBoardPayload payload, Level level) {
        final int size = payload.size();
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
            size,
            payload.states(),
            payload.goals(),
            playerTeams,
            payload.renderMode(),
            new GoalProgress[size * size]
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
            BingoClient.clientGame.getStates(), 0,
            BingoClient.clientGame.getSize() * BingoClient.clientGame.getSize()
        );
    }

    @Override
    public void handleSyncTeam(SyncTeamPayload payload) {
        BingoClient.clientTeam = BingoClient.receivedClientTeam = payload.team();
    }

    @Override
    public void handleUpdateProgress(UpdateProgressPayload payload) {
        if (!checkGamePresent(payload)) return;
        BingoClient.clientGame.getProgress()[payload.index()] = new GoalProgress(payload.progress(), payload.maxProgress());
    }

    @Override
    public void handleUpdateState(UpdateStatePayload payload) {
        if (!checkGamePresent(payload)) return;
        final int index = payload.index();
        if (index < 0 || index >= BingoClient.clientGame.getSize() * BingoClient.clientGame.getSize()) {
            Bingo.LOGGER.warn("Invalid {} payload: invalid board index {}", UpdateStatePayload.TYPE, index);
            return;
        }
        BingoClient.clientGame.getStates()[index] = payload.newState();
    }

    @Override
    public void handleUpdateEndTime(UpdateEndTimePayload payload) {
        if (!checkGamePresent(payload)) return;
        BingoClient.clientGame.setScheduledEndTime(payload.endTime());
    }

    private static boolean checkGamePresent(CustomPacketPayload payload) {
        if (BingoClient.clientGame == null) {
            Bingo.LOGGER.warn("BingoClient.clientGame == null while handling {}!", payload.type());
            return false;
        }
        return true;
    }
}
