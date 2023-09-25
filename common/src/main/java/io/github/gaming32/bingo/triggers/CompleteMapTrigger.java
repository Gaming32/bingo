package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CompleteMapTrigger extends SimpleCriterionTrigger<CompleteMapTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            MinMaxBounds.Ints.fromJson(json.get("scale")),
            json.has("locked") ? Optional.of(GsonHelper.getAsBoolean(json, "locked")) : Optional.empty(),
            LocationPredicate.fromJson(json.get("center"))
        );
    }

    public void trigger(ServerPlayer player, MapItemSavedData data) {
        trigger(player, instance -> instance.matches(player.serverLevel(), data));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints scale;
        private final Optional<Boolean> locked;
        private final Optional<LocationPredicate> center;

        public TriggerInstance(
            Optional<ContextAwarePredicate> player,
            MinMaxBounds.Ints scale,
            Optional<Boolean> locked,
            Optional<LocationPredicate> center
        ) {
            super(player);
            this.scale = scale;
            this.locked = locked;
            this.center = center;
        }

        public static TriggerInstance completeMap() {
            return new TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty());
        }

        public static TriggerInstance completeMap(MinMaxBounds.Ints scale) {
            return new TriggerInstance(Optional.empty(), scale, Optional.empty(), Optional.empty());
        }

        public static TriggerInstance completeMap(MinMaxBounds.Ints scale, LocationPredicate center) {
            return new TriggerInstance(Optional.empty(), scale, Optional.empty(), Optional.of(center));
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            result.add("scale", scale.serializeToJson());
            locked.ifPresent(p -> result.addProperty("locked", p));
            center.ifPresent(p -> result.add("center", p.serializeToJson()));
            return result;
        }

        public boolean matches(ServerLevel level, MapItemSavedData data) {
            if (!this.scale.matches(data.scale)) {
                return false;
            }
            if (this.locked.isPresent() && data.locked != this.locked.get()) {
                return false;
            }
            if (this.center.isEmpty()) {
                return true;
            }
            final ServerLevel centerLevel = level.getServer().getLevel(data.dimension);
            return centerLevel != null && this.center.get().matches(
                centerLevel, data.centerX, centerLevel.getSeaLevel(), data.centerZ
            );
        }
    }
}
