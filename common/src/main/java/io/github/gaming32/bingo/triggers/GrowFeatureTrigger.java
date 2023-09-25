package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.gaming32.bingo.ext.GlobalVars;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GrowFeatureTrigger extends SimpleCriterionTrigger<GrowFeatureTrigger.TriggerInstance> {
    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        LocationPredicate location = LocationPredicate.fromJson(json.get("location"));

        JsonElement featureJson = json.get("feature");
        TagPredicate<ConfiguredFeature<?, ?>> feature;
        if (featureJson == null || featureJson.isJsonNull()) {
            feature = null;
        } else {
            feature = TagPredicate.fromJson(featureJson, Registries.CONFIGURED_FEATURE);
        }

        return new TriggerInstance(player, location, feature);
    }

    public static boolean wrapPlaceOperation(
        ConfiguredFeature<?, ?> feature,
        WorldGenLevel level,
        ChunkGenerator chunkGen,
        RandomSource rand,
        BlockPos pos,
        Operation<Boolean> operation
    ) {
        boolean result = operation.call(feature, level, chunkGen, rand, pos);
        if (result) {
            if (GlobalVars.getCurrentPlayer() instanceof ServerPlayer player) {
                Registry<ConfiguredFeature<?, ?>> registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
                Holder<ConfiguredFeature<?, ?>> holder = registry.getHolderOrThrow(registry.getResourceKey(feature).orElseThrow());
                BingoTriggers.GROW_FEATURE.trigger(player, player.serverLevel(), pos, holder);
            }
        }
        return result;
    }

    public void trigger(ServerPlayer player, ServerLevel level, BlockPos pos, Holder<ConfiguredFeature<?, ?>> feature) {
        trigger(player, triggerInstance -> triggerInstance.matches(level, pos, feature));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;
        @Nullable
        private final TagPredicate<ConfiguredFeature<?, ?>> feature;

        public TriggerInstance(ContextAwarePredicate player, LocationPredicate location, @Nullable TagPredicate<ConfiguredFeature<?, ?>> feature) {
            super(ID, player);
            this.location = location;
            this.feature = feature;
        }

        @Override
        @NotNull
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("location", location.serializeToJson());
            if (feature != null) {
                json.add("feature", feature.serializeToJson());
            }
            return json;
        }

        public boolean matches(ServerLevel level, BlockPos pos, Holder<ConfiguredFeature<?, ?>> feature) {
            return location.matches(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                && (this.feature == null || this.feature.matches(feature));
        }
    }

    public static final class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private LocationPredicate location = LocationPredicate.ANY;
        @Nullable
        private TagPredicate<ConfiguredFeature<?, ?>> feature;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
            return this;
        }

        public Builder location(LocationPredicate location) {
            this.location = location;
            return this;
        }

        public Builder feature(TagPredicate<ConfiguredFeature<?, ?>> feature) {
            this.feature = feature;
            return this;
        }

        public Builder feature(TagKey<ConfiguredFeature<?, ?>> feature) {
            return feature(TagPredicate.is(feature));
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, location, feature);
        }
    }
}
