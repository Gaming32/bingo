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

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate arrow;
        private final BlockPredicate buttonOrPlate;
        private final LocationPredicate location;

        public TriggerInstance(
            ContextAwarePredicate predicate,
            EntityPredicate arrow,
            BlockPredicate buttonOrPlate,
            LocationPredicate location
        ) {
            super(ID, predicate);
            this.arrow = arrow;
            this.buttonOrPlate = buttonOrPlate;
            this.location = location;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            if (arrow != EntityPredicate.ANY) {
                result.add("arrow", arrow.serializeToJson());
            }
            if (buttonOrPlate != BlockPredicate.ANY) {
                result.add("button_or_plate", buttonOrPlate.serializeToJson());
            }
            if (location != LocationPredicate.ANY) {
                result.add("location", location.serializeToJson());
            }
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
}
