package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.VanillaNetworking;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPacket;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(ServerScoreboard.class)
public class MixinServerScoreboard {
    @Shadow @Final private MinecraftServer server;

    @Inject(
        method = "addPlayerToTeam",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V",
            shift = At.Shift.AFTER
        )
    )
    private void syncTeamAdd(String playerName, PlayerTeam team, CallbackInfoReturnable<Boolean> cir) {
        bingo$syncTeam(playerName);
    }

    @Inject(
        method = "removePlayerFromTeam",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V",
            shift = At.Shift.AFTER
        )
    )
    private void syncTeamRemove(String username, PlayerTeam playerTeam, CallbackInfo ci) {
        bingo$syncTeam(username);
    }

    @Unique
    private void bingo$syncTeam(String playerName) {
        if (Bingo.activeGame != null) {
            final ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
            if (player != null) {
                final BingoBoard.Teams team = Bingo.activeGame.getTeam(player);
                new SyncTeamPacket(team).sendTo(player);
                if (team.any()) {
                    Bingo.activeGame.flushQueuedGoals(player);
                }
                new ResyncStatesPacket(Bingo.activeGame.obfuscateTeam(team)).sendTo(player);
                player.connection.send(new ClientboundUpdateAdvancementsPacket(
                    false,
                    List.of(),
                    Set.of(),
                    VanillaNetworking.generateProgressMap(Bingo.activeGame.getBoard().getStates(), team)
                ));
            }
        }
    }
}
