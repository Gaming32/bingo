package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BounceOnBlockTrigger extends SimpleCriterionTrigger<BounceOnBlockTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, LocationPredicate.fromJson(json.get("block")));
    }

    public void trigger(ServerPlayer player, BlockPos blockPos) {
        trigger(player, instance -> instance.matches(player.serverLevel(), blockPos));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<LocationPredicate> block;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> block) {
            super(player);
            this.block = block;
        }

        public static Criterion<TriggerInstance> bounceOnBlock(BlockPredicate.Builder block) {
            return BingoTriggers.BOUNCE_ON_BLOCK.createCriterion(new TriggerInstance(
                Optional.empty(), Optional.of(LocationPredicate.Builder.location().setBlock(block).build())
            ));
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            block.ifPresent(p -> result.add("block", p.serializeToJson()));
            return result;
        }

        public boolean matches(ServerLevel level, BlockPos pos) {
            return block.isEmpty() || block.get().matches(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }
    }
}
