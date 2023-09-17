package io.github.gaming32.bingo.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PillarCondition implements LootItemCondition {
    private final int minHeight;
    private final BlockPredicate block;

    public PillarCondition(int minHeight, BlockPredicate block) {
        this.minHeight = minHeight;
        this.block = block;
    }

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.PILLAR.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        final ServerLevel level = lootContext.getLevel();
        final BlockPos.MutableBlockPos pos = BlockPos.containing(lootContext.getParam(LootContextParams.ORIGIN)).mutable();

        int height = 0;
        while (true) {
            if (!block.matches(level, pos)) {
                return false;
            }
            height++;
            if (height >= minHeight) {
                return true;
            }
            pos.move(0, 1, 0);
        }
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<PillarCondition> {
        @Override
        public void serialize(JsonObject json, PillarCondition value, JsonSerializationContext serializationContext) {
            json.addProperty("min_height", value.minHeight);
            json.add("block", value.block.serializeToJson());
        }

        @NotNull
        @Override
        public PillarCondition deserialize(JsonObject json, JsonDeserializationContext serializationContext) {
            return new PillarCondition(
                GsonHelper.getAsInt(json, "min_height"),
                BlockPredicate.fromJson(json.get("block"))
            );
        }
    }
}
