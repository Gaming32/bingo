package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.network.messages.s2c.InitBoardPacket;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPacket;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPacket;
import io.github.gaming32.bingo.network.messages.s2c.UpdateProgressPacket;
import io.github.gaming32.bingo.network.messages.s2c.UpdateStatePacket;
import io.github.gaming32.bingo.platform.BingoPlatform;
import net.minecraft.world.level.Level;

public interface ClientPayloadHandler {
    void handleInitBoard(InitBoardPacket payload, Level level);

    void handleRemoveBoard();

    void handleResyncStates(ResyncStatesPacket packet);

    void handleSyncTeam(SyncTeamPacket packet);

    void handleUpdateProgress(UpdateProgressPacket packet);

    void handleUpdateState(UpdateStatePacket packet);

    static ClientPayloadHandler get() {
        if (Holder.instance == null) {
            if (BingoPlatform.platform.isClient()) {
                throw new IllegalStateException("ClientPayloadHandler not initialized yet!");
            }
            throw new IllegalStateException("Cannot call ClientPayloadHandler.get() on server!");
        }
        return Holder.instance;
    }

    static void init(ClientPayloadHandler instance) {
        if (Holder.instance != null) {
            throw new IllegalStateException("Cannot call ClientPayloadHandler.init() more than once.");
        }
        if (!BingoPlatform.platform.isClient()) {
            throw new IllegalStateException("Cannot call ClientPayloadHandler.init() on server.");
        }
        Holder.instance = instance;
    }

    class Holder {
        private static ClientPayloadHandler instance;
    }
}
