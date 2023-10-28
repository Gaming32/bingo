package io.github.gaming32.bingo.mixin.common;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ContextAwarePredicate.class)
public interface ContextAwarePredicateAccessor {
    @Invoker("<init>")
    static ContextAwarePredicate create(List<LootItemCondition> list) {
        throw new AssertionError();
    }

    @Accessor
    List<LootItemCondition> getConditions();
}
