package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.github.gaming32.bingo.network.messages.s2c.InitBoardPacket;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPacket;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPacket;
import io.github.gaming32.bingo.network.messages.s2c.UpdateProgressPacket;
import io.github.gaming32.bingo.network.messages.s2c.UpdateStatePacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class ClientPayloadHandlerImpl implements ClientPayloadHandler {
    @Override
    public void handleInitBoard(InitBoardPacket packet, Level level) {
        final int size = packet.size();
        final String[] teams = packet.teams();

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
            packet.states(),
            packet.goals(),
            playerTeams,
            packet.renderMode(),
            new GoalProgress[size * size]
        );
    }

    @Override
    public void handleRemoveBoard() {
        BingoClient.clientGame = null;
    }

    @Override
    public void handleResyncStates(ResyncStatesPacket packet) {
        if (!checkGamePresent(packet)) return;
        System.arraycopy(
            packet.states(), 0,
            BingoClient.clientGame.states(), 0,
            BingoClient.clientGame.size() * BingoClient.clientGame.size()
        );
    }

    @Override
    public void handleSyncTeam(SyncTeamPacket packet) {
        BingoClient.clientTeam = BingoClient.receivedClientTeam = packet.team();
    }

    @Override
    public void handleUpdateProgress(UpdateProgressPacket packet) {
        if (!checkGamePresent(packet)) return;
        BingoClient.clientGame.progress()[packet.index()] = new GoalProgress(packet.progress(), packet.maxProgress());
    }

    @Override
    public void handleUpdateState(UpdateStatePacket packet) {
        if (!checkGamePresent(packet)) return;
        final int index = packet.index();
        if (index < 0 || index >= BingoClient.clientGame.size() * BingoClient.clientGame.size()) {
            Bingo.LOGGER.warn("Invalid {} packet: invalid board index {}", UpdateStatePacket.TYPE, index);
            return;
        }
        BingoClient.clientGame.states()[index] = packet.newState();
    }

    private static boolean checkGamePresent(CustomPacketPayload payload) {
        if (BingoClient.clientGame == null) {
            Bingo.LOGGER.warn("BingoClient.clientGame == null while handling {}!", payload.type());
            return false;
        }
        return true;
    }
}
