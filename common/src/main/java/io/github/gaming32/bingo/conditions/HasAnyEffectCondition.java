package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record HasAnyEffectCondition(LootContext.EntityTarget entityTarget) implements LootItemCondition {
    public static final MapCodec<HasAnyEffectCondition> CODEC = LootContext.EntityTarget.CODEC
        .xmap(HasAnyEffectCondition::new, HasAnyEffectCondition::entityTarget)
        .fieldOf("entity");

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.HAS_ANY_EFFECT.get();
    }

    @NotNull
    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(entityTarget.getParam());
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.getParameter(entityTarget.getParam()) instanceof LivingEntity livingEntity
            && !livingEntity.getActiveEffectsMap().isEmpty();
    }
}
