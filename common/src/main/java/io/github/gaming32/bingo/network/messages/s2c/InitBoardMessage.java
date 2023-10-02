package io.github.gaming32.bingo.network.messages.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.ClientBoard;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.ClientGoal;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Arrays;

public class InitBoardMessage extends BaseS2CMessage {
    private final int size;
    private final ClientGoal[] goals;
    private final BingoBoard.Teams[] states;

    public InitBoardMessage(int size, ActiveGoal[] goals, BingoBoard.Teams[] states) {
        this.size = size;
        this.goals = new ClientGoal[goals.length];
        this.states = states;

        for (int i = 0; i < goals.length; i++) {
            this.goals[i] = new ClientGoal(goals[i]);
        }
    }

    public InitBoardMessage(FriendlyByteBuf buf) {
        size = buf.readVarInt();
        goals = buf.readList(ClientGoal::new).toArray(ClientGoal[]::new);
        states = buf.readList(b -> BingoBoard.Teams.fromBits(b.readVarInt())).toArray(BingoBoard.Teams[]::new);
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
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BingoClient.clientBoard = new ClientBoard(size, states, goals);
    }
}
