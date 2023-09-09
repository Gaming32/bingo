package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.PlayerPredicateBuilderExt;
import io.github.gaming32.bingo.ext.PlayerPredicateExt;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.stats.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(PlayerPredicate.Builder.class)
public class MixinPlayerPredicate_Builder implements PlayerPredicateBuilderExt {
    @Unique
    private final Map<Stat<?>, MinMaxBounds.Ints> bingo$relativeStats = new HashMap<>();

    @Override
    public PlayerPredicate.Builder bingo$addRelativeStat(Stat<?> stat, MinMaxBounds.Ints value) {
        bingo$relativeStats.put(stat, value);
        return (PlayerPredicate.Builder)(Object)this;
    }

    @Inject(method = "build", at = @At("RETURN"))
    private void appendCustomFields(CallbackInfoReturnable<PlayerPredicate> cir) {
        if (!bingo$relativeStats.isEmpty()) {
            ((PlayerPredicateExt)cir.getReturnValue()).bingo$setRelativeStats(bingo$relativeStats);
        }
    }
}
