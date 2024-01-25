package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class RemoveBoardPacket extends AbstractCustomPayload {
    public static final ResourceLocation ID = id("remove_board");
    public static final RemoveBoardPacket INSTANCE = new RemoveBoardPacket();

    private RemoveBoardPacket() {
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        BingoClient.clientGame = null;
    }
}
