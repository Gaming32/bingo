package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.NotNull;

public class ArrowPressTrigger extends SimpleCriterionTrigger<ArrowPressTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:arrow_press");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(
            predicate,
            EntityPredicate.fromJson(json.get("arrow")),
            BlockPredicate.fromJson(json.get("button_or_plate")),
            LocationPredicate.fromJson(json.get("location"))
        );
    }

    public void trigger(AbstractArrow arrow, BlockPos pos) {
        if (!(arrow.getOwner() instanceof ServerPlayer player)) return;
        trigger(player, instance -> instance.matches(player, arrow, pos));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate arrow;
        private final BlockPredicate buttonOrPlate;
        private final LocationPredicate location;

        public TriggerInstance(
            ContextAwarePredicate player,
            EntityPredicate arrow,
            BlockPredicate buttonOrPlate,
            LocationPredicate location
        ) {
            super(ID, player);
            this.arrow = arrow;
            this.buttonOrPlate = buttonOrPlate;
            this.location = location;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("arrow", arrow.serializeToJson());
            result.add("button_or_plate", buttonOrPlate.serializeToJson());
            result.add("location", location.serializeToJson());
            return result;
        }

        public boolean matches(ServerPlayer player, AbstractArrow arrow, BlockPos pos) {
            if (!this.arrow.matches(player, arrow)) {
                return false;
            }
            if (!this.buttonOrPlate.matches(player.serverLevel(), pos)) {
                return false;
            }
            if (!this.location.matches(player.serverLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private EntityPredicate arrow = EntityPredicate.ANY;
        private BlockPredicate buttonOrPlate = BlockPredicate.ANY;
        private LocationPredicate location = LocationPredicate.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
            return this;
        }

        public Builder arrow(EntityPredicate arrow) {
            this.arrow = arrow;
            return this;
        }

        public Builder buttonOrPlate(BlockPredicate buttonOrPlate) {
            this.buttonOrPlate = buttonOrPlate;
            return this;
        }

        public Builder location(LocationPredicate location) {
            this.location = location;
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, arrow, buttonOrPlate, location);
        }
    }
}
