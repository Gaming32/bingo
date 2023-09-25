package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.mixin.common.LocationCheckAccessor;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
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

import java.util.Optional;

public class BreakBlockTrigger extends SimpleCriterionTrigger<BreakBlockTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, BingoUtil.getAdvancementLocation(json, "location", context));
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
        final LootContext location = new LootContext.Builder(locationParams).create(Optional.empty());
        trigger(player, triggerInstance -> triggerInstance.matches(location));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> location;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location) {
            super(player);
            this.location = location;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            location.ifPresent(p -> result.add("location", p.toJson()));
            return result;
        }

        public boolean matches(LootContext location) {
            return this.location.isEmpty() || this.location.get().matches(location);
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> location = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder location(ContextAwarePredicate location) {
            this.location = Optional.ofNullable(location);
            return this;
        }

        public Builder location(LootItemCondition... conditions) {
            return location(ContextAwarePredicate.create(conditions));
        }

        public Builder location(LocationPredicate location) {
            return location(LocationCheckAccessor.createLocationCheck(location, BlockPos.ZERO));
        }

        public Builder block(BlockPredicate.Builder block) {
            return location(LocationPredicate.Builder.location().setBlock(block).build());
        }

        public Builder block(Block block) {
            return block(BlockPredicate.Builder.block().of(block));
        }

        public Builder block(TagKey<Block> blockTag) {
            return block(BlockPredicate.Builder.block().of(blockTag));
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, location);
        }
    }
}
