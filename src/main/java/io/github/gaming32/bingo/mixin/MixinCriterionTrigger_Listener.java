package io.github.gaming32.bingo.mixin;

import io.github.gaming32.bingo.ext.MinecraftServerExt;
import io.github.gaming32.bingo.game.ActiveGoal;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CriterionTrigger.Listener.class)
public class MixinCriterionTrigger_Listener {
    @Shadow @Final private AdvancementHolder advancement;

    @Shadow @Final private String criterion;

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void listenForGoalCompletion(PlayerAdvancements player, CallbackInfo ci) {
        final var serverPlayer = ((PlayerAdvancementsAccessor) player).getPlayer();
        MinecraftServer server = serverPlayer.level().getServer();
        final var game = ((MinecraftServerExt) server).bingo$getGame();
        if (game == null) return;
        final ActiveGoal goal = game.getBoard().byVanillaId(advancement.id());
        if (goal == null) return;
        game.award(serverPlayer, goal, criterion);
        ci.cancel();
    }
}
