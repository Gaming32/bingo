package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

public class CompleteMapTrigger extends SimpleCriterionTrigger<CompleteMapTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:completed_map");

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
            MinMaxBounds.Ints.fromJson(json.get("scale")),
            json.has("locked") ? GsonHelper.getAsBoolean(json, "locked") : null,
            LocationPredicate.fromJson(json.get("center"))
        );
    }

    public void trigger(ServerPlayer player, MapItemSavedData data) {
        trigger(player, instance -> instance.matches(player.serverLevel(), data));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints scale;
        private final Boolean locked;
        private final LocationPredicate center;

        public TriggerInstance(ContextAwarePredicate predicate, MinMaxBounds.Ints scale, Boolean locked, LocationPredicate center) {
            super(ID, predicate);
            this.scale = scale;
            this.locked = locked;
            this.center = center;
        }

        public static TriggerInstance completeMap() {
            return new TriggerInstance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.ANY, null, LocationPredicate.ANY);
        }

        public static TriggerInstance completeMap(MinMaxBounds.Ints scale) {
            return new TriggerInstance(ContextAwarePredicate.ANY, scale, null, LocationPredicate.ANY);
        }

        public static TriggerInstance completeMap(MinMaxBounds.Ints scale, LocationPredicate center) {
            return new TriggerInstance(ContextAwarePredicate.ANY, scale, null, center);
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("scale", scale.serializeToJson());
            result.addProperty("locked", locked);
            result.add("center", center.serializeToJson());
            return result;
        }

        public boolean matches(ServerLevel level, MapItemSavedData data) {
            if (!this.scale.matches(data.scale)) {
                return false;
            }
            if (this.locked != null && data.locked != this.locked) {
                return false;
            }
            if (this.center == LocationPredicate.ANY) {
                return true;
            }
            final ServerLevel centerLevel = level.getServer().getLevel(data.dimension);
            return centerLevel != null && this.center.matches(
                centerLevel, data.centerX, centerLevel.getSeaLevel(), data.centerZ
            );
        }
    }
}
