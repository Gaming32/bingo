package io.github.gaming32.bingo.mixin.common;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LocationCheck.class)
public interface LocationCheckAccessor {
    @Invoker("<init>")
    static LocationCheck createLocationCheck(LocationPredicate locationPredicate, BlockPos blockPos) {
        throw new AssertionError();
    }
}
