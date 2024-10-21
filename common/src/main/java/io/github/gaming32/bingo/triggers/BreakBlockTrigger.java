package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
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
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BreakBlockTrigger extends SimpleCriterionTrigger<BreakBlockTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
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

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> location
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(LootContext location) {
            return this.location.isEmpty() || this.location.get().matches(location);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            location.ifPresent(p -> criterionValidator.validate(p, LootContextParamSets.ADVANCEMENT_LOCATION, ".location"));
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
            return location(new LocationCheck(Optional.ofNullable(location), BlockPos.ZERO));
        }

        public Builder block(BlockPredicate.Builder block) {
            return location(LocationPredicate.Builder.location().setBlock(block).build());
        }

        public Builder block(HolderGetter<Block> blocks, Block block) {
            return block(BlockPredicate.Builder.block().of(blocks, block));
        }

        public Builder block(HolderGetter<Block> blocks, TagKey<Block> blockTag) {
            return block(BlockPredicate.Builder.block().of(blocks, blockTag));
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.BREAK_BLOCK.get().createCriterion(new TriggerInstance(player, location));
        }
    }
}
