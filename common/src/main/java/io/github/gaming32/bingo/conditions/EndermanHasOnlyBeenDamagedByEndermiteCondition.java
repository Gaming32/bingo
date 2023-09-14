package io.github.gaming32.bingo.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.gaming32.bingo.ext.EnderManExt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public class EndermanHasOnlyBeenDamagedByEndermiteCondition implements LootItemCondition {
    @Override
    @NotNull
    public LootItemConditionType getType() {
        return BingoConditions.ENDERMAN_HAS_ONLY_BEEN_DAMAGED_BY_ENDERMITE.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        return entity instanceof EnderManExt enderman && enderman.bingo$hasOnlyBeenDamagedByEndermite();
    }

    public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EndermanHasOnlyBeenDamagedByEndermiteCondition> {
        @Override
        public void serialize(JsonObject json, EndermanHasOnlyBeenDamagedByEndermiteCondition value, JsonSerializationContext serializationContext) {
        }

        @Override
        @NotNull
        public EndermanHasOnlyBeenDamagedByEndermiteCondition deserialize(JsonObject json, JsonDeserializationContext serializationContext) {
            return new EndermanHasOnlyBeenDamagedByEndermiteCondition();
        }
    }
}
