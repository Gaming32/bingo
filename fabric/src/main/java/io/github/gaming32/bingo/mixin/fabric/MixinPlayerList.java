package io.github.gaming32.bingo.mixin.fabric;

import io.github.gaming32.bingo.fabric.FabricEvents;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class MixinPlayerList {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onPlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        FabricEvents.PLAYER_JOIN.invoker().accept(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerQuit(ServerPlayer player, CallbackInfo ci) {
        FabricEvents.PLAYER_QUIT.invoker().accept(player);
    }
}
