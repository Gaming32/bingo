package io.github.gaming32.bingo.triggers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.ext.GlobalVars;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.TagPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GrowFeatureTrigger extends SimpleCriterionTrigger<GrowFeatureTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public static boolean wrapPlaceOperation(
        Feature feature,
        WorldGenLevel level,
        ChunkGenerator chunkGen,
        RandomSource rand,
        BlockPos pos,
        Operation<Boolean> operation
    ) {
        boolean result = operation.call(feature, level, chunkGen, rand, pos);
        if (result && GlobalVars.CURRENT_PLAYER.get() instanceof ServerPlayer player) {
            Registry<Feature> registry = level.registryAccess().lookupOrThrow(Registries.FEATURE);
            Holder<Feature> holder = registry.getOrThrow(registry.getResourceKey(feature).orElseThrow());
            BingoTriggers.GROW_FEATURE.get().trigger(player, player.level(), pos, holder);
        }
        return result;
    }

    public void trigger(ServerPlayer player, ServerLevel level, BlockPos pos, Holder<Feature> feature) {
        trigger(player, triggerInstance -> triggerInstance.matches(level, pos, feature));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<Holder<LootItemCondition>> player,
        Optional<LocationPredicate> location,
        List<TagPredicate<Feature>> tags
    ) implements SimpleInstance {
        private static final Codec<List<TagPredicate<Feature>>> TAGS_CODEC =
            TagPredicate.codec(Registries.FEATURE).listOf();
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location),
                TAGS_CODEC.optionalFieldOf("tags", List.of()).forGetter(TriggerInstance::tags)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerLevel level, BlockPos pos, Holder<Feature> feature) {
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
        private Optional<Holder<LootItemCondition>> player = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private final List<TagPredicate<Feature>> tags = new ArrayList<>();

        private Builder() {
        }

        public Builder player(Holder<LootItemCondition> player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder location(LocationPredicate location) {
            this.location = Optional.ofNullable(location);
            return this;
        }

        public Builder feature(TagPredicate<Feature> feature) {
            this.tags.add(feature);
            return this;
        }

        public Builder feature(HolderGetter<Feature> features, TagKey<Feature> feature) {
            return feature(TagPredicate.is(features, feature));
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.GROW_FEATURE.get().createCriterion(
                new TriggerInstance(player, location, List.copyOf(tags))
            );
        }
    }
}
