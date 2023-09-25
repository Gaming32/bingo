package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.ext.GlobalVars;
import io.github.gaming32.bingo.util.BingoUtil;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GrowFeatureTrigger extends SimpleCriterionTrigger<GrowFeatureTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            LocationPredicate.fromJson(json.get("location")),
            json.has("tags") ? BingoUtil.fromJsonElement(TriggerInstance.TAGS_CODEC, json.get("tags")) : List.of()
        );
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
        private static final Codec<List<TagPredicate<ConfiguredFeature<?, ?>>>> TAGS_CODEC =
            TagPredicate.codec(Registries.CONFIGURED_FEATURE).listOf();

        private final Optional<LocationPredicate> location;
        private final List<TagPredicate<ConfiguredFeature<?, ?>>> tags;

        public TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<LocationPredicate> location,
            List<TagPredicate<ConfiguredFeature<?, ?>>> tags
        ) {
            super(player);
            this.location = location;
            this.tags = tags;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            location.ifPresent(p -> result.add("location", p.serializeToJson()));
            if (!tags.isEmpty()) {
                result.add("tags", BingoUtil.toJsonElement(TAGS_CODEC, tags));
            }
            return result;
        }

        public boolean matches(ServerLevel level, BlockPos pos, Holder<ConfiguredFeature<?, ?>> feature) {
            if (location.isPresent() && !location.get().matches(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                return false;
            }
            for (final var tag : tags) {
                if (!tag.matches(feature)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private final List<TagPredicate<ConfiguredFeature<?, ?>>> tags = new ArrayList<>();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder location(LocationPredicate location) {
            this.location = Optional.ofNullable(location);
            return this;
        }

        public Builder feature(TagPredicate<ConfiguredFeature<?, ?>> feature) {
            this.tags.add(feature);
            return this;
        }

        public Builder feature(TagKey<ConfiguredFeature<?, ?>> feature) {
            return feature(TagPredicate.is(feature));
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, location, List.copyOf(tags));
        }
    }
}
