package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.CriterionTriggerExt;
import net.minecraft.advancements.CriterionTrigger;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CriterionTrigger.class)
public interface MixinCriterionTrigger extends CriterionTriggerExt {
}
