package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, BingoUtil.getAdvancementLocation(json, "nest", context));
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

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> nest;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> nest) {
            super(player);
            this.nest = nest;
        }

        public static Criterion<TriggerInstance> grew() {
            return BingoTriggers.GROW_BEE_NEST_TREE.createCriterion(
                new TriggerInstance(Optional.empty(), Optional.empty())
            );
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            nest.ifPresent(p -> result.add("nest", p.toJson()));
            return result;
        }

        public boolean matches(LootContext nest) {
            return this.nest.isEmpty() || this.nest.get().matches(nest);
        }
    }
}
