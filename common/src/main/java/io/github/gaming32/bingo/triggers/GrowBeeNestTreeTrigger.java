package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GrowBeeNestTreeTrigger extends SimpleCriterionTrigger<GrowBeeNestTreeTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos nestPos, BlockState nestState, ItemStack tool) {
        final LootParams nestParams = new LootParams.Builder(player.serverLevel())
            .withParameter(LootContextParams.ORIGIN, nestPos.getCenter())
            .withParameter(LootContextParams.THIS_ENTITY, player)
            .withParameter(LootContextParams.BLOCK_STATE, nestState)
            .withParameter(LootContextParams.TOOL, tool)
            .create(LootContextParamSets.ADVANCEMENT_LOCATION);
        final LootContext nest = new LootContext.Builder(nestParams).create(Optional.empty());
        trigger(player, instance -> instance.matches(nest));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> nest
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(ContextAwarePredicate.CODEC, "nest").forGetter(TriggerInstance::nest)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> grew() {
            return BingoTriggers.GROW_BEE_NEST_TREE.createCriterion(
                new TriggerInstance(Optional.empty(), Optional.empty())
            );
        }

        public boolean matches(LootContext nest) {
            return this.nest.isEmpty() || this.nest.get().matches(nest);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            nest.ifPresent(p -> criterionValidator.validate(p, LootContextParamSets.ADVANCEMENT_LOCATION, ".nest"));
        }
    }
}
