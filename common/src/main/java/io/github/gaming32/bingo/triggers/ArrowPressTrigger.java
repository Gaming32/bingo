package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ArrowPressTrigger extends SimpleCriterionTrigger<ArrowPressTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            EntityPredicate.fromJson(json, "arrow", context),
            BingoUtil.fromOptionalJsonElement(BlockPredicate.CODEC, json.get("button_or_plate")),
            LocationPredicate.fromJson(json.get("location"))
        );
    }

    public void trigger(AbstractArrow arrow, BlockPos pos) {
        if (!(arrow.getOwner() instanceof ServerPlayer player)) return;
        final LootContext arrowContext = EntityPredicate.createContext(player, arrow);
        trigger(player, instance -> instance.matches(arrowContext, pos));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> arrow;
        private final Optional<BlockPredicate> buttonOrPlate;
        private final Optional<LocationPredicate> location;

        public TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ContextAwarePredicate> arrow,
            Optional<BlockPredicate> buttonOrPlate,
            Optional<LocationPredicate> location
        ) {
            super(player);
            this.arrow = arrow;
            this.buttonOrPlate = buttonOrPlate;
            this.location = location;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            arrow.ifPresent(p -> result.add("arrow", p.toJson()));
            buttonOrPlate.ifPresent(p -> result.add("button_or_plate", BingoUtil.toJsonObject(BlockPredicate.CODEC, p)));
            location.ifPresent(p -> result.add("location", p.serializeToJson()));
            return result;
        }

        public boolean matches(LootContext arrow, BlockPos pos) {
            if (this.arrow.isPresent() && !this.arrow.get().matches(arrow)) {
                return false;
            }
            if (this.buttonOrPlate.isPresent() && !this.buttonOrPlate.get().matches(arrow.getLevel(), pos)) {
                return false;
            }
            if (
                this.location.isPresent() &&
                    !this.location.get().matches(arrow.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
            ) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> arrow = Optional.empty();
        private Optional<BlockPredicate> buttonOrPlate = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder arrow(ContextAwarePredicate arrow) {
            this.arrow = Optional.ofNullable(arrow);
            return this;
        }

        public Builder arrow(EntityPredicate arrow) {
            this.arrow = EntityPredicate.wrap(Optional.ofNullable(arrow));
            return this;
        }

        public Builder buttonOrPlate(BlockPredicate buttonOrPlate) {
            this.buttonOrPlate = Optional.ofNullable(buttonOrPlate);
            return this;
        }

        public Builder location(LocationPredicate location) {
            this.location = Optional.ofNullable(location);
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, arrow, buttonOrPlate, location);
        }
    }
}
