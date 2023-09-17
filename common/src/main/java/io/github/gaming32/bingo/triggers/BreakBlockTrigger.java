package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.mixin.common.LocationCheckAccessor;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class BreakBlockTrigger extends SimpleCriterionTrigger<BreakBlockTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation(Bingo.MOD_ID, "break_block");

    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, EntityPredicate.fromJson(json, "location", context));
    }

    @Override
    @NotNull
    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player, BlockPos pos, ItemStack tool) {
        final ServerLevel level = player.serverLevel();
        final BlockState state = level.getBlockState(pos);
        final LootParams locationParams = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, pos.getCenter())
            .withParameter(LootContextParams.THIS_ENTITY, player)
            .withParameter(LootContextParams.BLOCK_STATE, state)
            .withParameter(LootContextParams.TOOL, tool)
            .create(LootContextParamSets.ADVANCEMENT_LOCATION);
        final LootContext location = new LootContext.Builder(locationParams).create(null);

        trigger(player, triggerInstance -> triggerInstance.matches(location));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate location;

        public TriggerInstance(ContextAwarePredicate player, ContextAwarePredicate location) {
            super(ID, player);
            this.location = location;
        }

        @Override
        @NotNull
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("location", location.toJson(context));
            return json;
        }

        public boolean matches(LootContext location) {
            return this.location.matches(location);
        }
    }

    public static final class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private ContextAwarePredicate location = ContextAwarePredicate.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
            return this;
        }

        public Builder location(ContextAwarePredicate location) {
            this.location = location;
            return this;
        }

        public Builder location(LootItemCondition... conditions) {
            return location(ContextAwarePredicate.create(conditions));
        }

        public Builder location(LocationPredicate location) {
            return location(LocationCheckAccessor.createLocationCheck(location, BlockPos.ZERO));
        }

        public Builder block(BlockPredicate block) {
            return location(LocationPredicate.Builder.location().setBlock(block).build());
        }

        public Builder block(Block block) {
            return block(BlockPredicate.Builder.block().of(block).build());
        }

        public Builder block(TagKey<Block> blockTag) {
            return block(BlockPredicate.Builder.block().of(blockTag).build());
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, location);
        }
    }
}
