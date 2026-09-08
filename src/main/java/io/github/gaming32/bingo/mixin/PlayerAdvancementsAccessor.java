package io.github.gaming32.bingo.mixin;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerAdvancements.class)
public interface PlayerAdvancementsAccessor {
    @Accessor
    ServerPlayer getPlayer();

    @Accessor
    boolean getIsFirstPacket();

    @Invoker
    <T extends CriterionTriggerInstance> void callAddListener(Criterion<T> typeAndInstance, PlayerAdvancements.TriggerInstanceKey criterion);

    @Invoker
    <T extends CriterionTriggerInstance> void callRemoveListener(final CriterionTrigger<T> type, final PlayerAdvancements.TriggerInstanceKey criterion);
}
