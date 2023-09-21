package io.github.gaming32.bingo.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class HasAnyEffectCondition implements LootItemCondition {
    private final LootContext.EntityTarget entityTarget;

    public HasAnyEffectCondition(LootContext.EntityTarget entityTarget) {
        this.entityTarget = entityTarget;
    }

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.HAS_ANY_EFFECT.get();
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(entityTarget.getParam());
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.getParam(entityTarget.getParam()) instanceof LivingEntity livingEntity
            && !livingEntity.getActiveEffectsMap().isEmpty();
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<HasAnyEffectCondition> {
        @Override
        public void serialize(JsonObject json, HasAnyEffectCondition value, JsonSerializationContext serializationContext) {
            json.add("entity", serializationContext.serialize(value.entityTarget));
        }

        @NotNull
        @Override
        public HasAnyEffectCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return new HasAnyEffectCondition(GsonHelper.getAsObject(json, "entity", context, LootContext.EntityTarget.class));
        }
    }
}
