package io.github.gaming32.bingo.mixin.common;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemPredicate.class)
public interface ItemPredicateAccessor {
    @Accessor
    MinMaxBounds.Ints getCount();

    @Mutable
    @Accessor
    void setCount(MinMaxBounds.Ints count);
}
