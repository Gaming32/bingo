package io.github.gaming32.bingo.mixin.common;

import net.minecraft.advancements.AdvancementProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AdvancementProgress.class)
public interface AdvancementProgressAccessor {
    @Invoker
    int callCountCompletedRequirements();
}
