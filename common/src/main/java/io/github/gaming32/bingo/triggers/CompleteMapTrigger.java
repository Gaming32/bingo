package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CompleteMapTrigger extends SimpleCriterionTrigger<CompleteMapTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, MapItemSavedData data) {
        trigger(player, instance -> instance.matches(player.serverLevel(), data));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        MinMaxBounds.Ints scale,
        Optional<Boolean> locked,
        Optional<LocationPredicate> center
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "scale", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::scale),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "locked").forGetter(TriggerInstance::locked),
                ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "center").forGetter(TriggerInstance::center)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> completeMap() {
            return BingoTriggers.COMPLETED_MAP.get().createCriterion(
                new TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty())
            );
        }

        public static Criterion<TriggerInstance> completeMap(MinMaxBounds.Ints scale) {
            return BingoTriggers.COMPLETED_MAP.get().createCriterion(
                new TriggerInstance(Optional.empty(), scale, Optional.empty(), Optional.empty())
            );
        }

        public static Criterion<TriggerInstance> completeMap(MinMaxBounds.Ints scale, LocationPredicate center) {
            return BingoTriggers.COMPLETED_MAP.get().createCriterion(
                new TriggerInstance(Optional.empty(), scale, Optional.empty(), Optional.of(center))
            );
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
