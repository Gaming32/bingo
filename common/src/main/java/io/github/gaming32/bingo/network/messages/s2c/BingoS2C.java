package io.github.gaming32.bingo.network.messages.s2c;

import dev.architectury.networking.simple.MessageType;

import static io.github.gaming32.bingo.network.BingoNetwork.NETWORK_MANAGER;

public class BingoS2C {
    public static final MessageType REMOVE_BOARD = NETWORK_MANAGER.registerS2C("remove_board", buf -> RemoveBoardMessage.INSTANCE);
    public static final MessageType UPDATE_STATE = NETWORK_MANAGER.registerS2C("update_state", UpdateStateMessage::new);
    public static final MessageType INIT_BOARD = NETWORK_MANAGER.registerS2C("init_board", InitBoardMessage::new);
    public static final MessageType SYNC_TEAM = NETWORK_MANAGER.registerS2C("sync_team", SyncTeamMessage::new);
    public static final MessageType RESYNC_STATES = NETWORK_MANAGER.registerS2C("resync_states", ResyncStatesMessage::new);

    public static void load() {
    }
}
