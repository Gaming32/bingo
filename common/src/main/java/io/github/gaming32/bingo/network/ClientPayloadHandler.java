package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.network.messages.s2c.InitBoardPayload;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPayload;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateEndTimePayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateProgressPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateStatePayload;
import io.github.gaming32.bingo.platform.BingoPlatform;
import net.minecraft.world.level.Level;

public interface ClientPayloadHandler {
    void handleInitBoard(InitBoardPayload payload, Level level);

    void handleRemoveBoard();

    void handleResyncStates(ResyncStatesPayload payload);

    void handleSyncTeam(SyncTeamPayload payload);

    void handleUpdateProgress(UpdateProgressPayload payload);

    void handleUpdateState(UpdateStatePayload payload);

    void handleUpdateEndTime(UpdateEndTimePayload payload);

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
