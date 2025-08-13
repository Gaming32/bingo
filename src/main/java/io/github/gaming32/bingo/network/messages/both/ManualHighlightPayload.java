package io.github.gaming32.bingo.network.messages.both;

import io.github.gaming32.bingo.ext.MinecraftServerExt;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record ManualHighlightPayload(int slot, int value, int modCount) implements AbstractCustomPayload {
    public static final CustomPacketPayload.Type<ManualHighlightPayload> TYPE = AbstractCustomPayload.type("manual_highlight");
    public static final StreamCodec<RegistryFriendlyByteBuf, ManualHighlightPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ManualHighlightPayload::slot,
        ByteBufCodecs.VAR_INT, ManualHighlightPayload::value,
        ByteBufCodecs.VAR_INT, ManualHighlightPayload::modCount,
        ManualHighlightPayload::new
    );

    @Override
    @NotNull
    public Type<ManualHighlightPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            ClientPayloadHandler.get().handleManualHighlight(this);
        } else {
            if (value < 0 || value > BingoBoard.NUM_MANUAL_HIGHLIGHT_COLORS) {
                return;
            }
            ServerPlayer player = (ServerPlayer) context.player();
            assert player != null;
            MinecraftServer server = player.getServer();
            assert server != null;
            BingoGame game = ((MinecraftServerExt) server).bingo$getGame();
            if (game == null) {
                return;
            }
            if (slot < 0 || slot >= game.getBoard().getShape().getGoalCount(game.getBoard().getSize())) {
                return;
            }
            BingoBoard.Teams team = game.getTeam(player);
            if (!team.one()) {
                return;
            }
            if (modCount != game.getBoard().getManualHighlightModCount(team)) {
                return;
            }
            game.getBoard().setTeamManualHighlight(server, game, team, slot, value == 0 ? null : value - 1, player);
        }
    }
}
