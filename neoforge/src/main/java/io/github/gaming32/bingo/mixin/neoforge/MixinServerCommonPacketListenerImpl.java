package io.github.gaming32.bingo.mixin.neoforge;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerCommonPacketListenerImpl.class)
public class MixinServerCommonPacketListenerImpl {
    @WrapWithCondition(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/network/registration/NetworkRegistry;checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ServerCommonPacketListener;)V"
        ),
        require = 0
    )
    private boolean dontCheckBingoPayloads(Packet<?> packet, ServerCommonPacketListener listener) {
        return !(packet instanceof ClientboundCustomPayloadPacket payloadPacket) ||
               !payloadPacket.payload().type().id().getNamespace().equals("bingo");
    }
}
