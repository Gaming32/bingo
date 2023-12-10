package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class IntentionalGameDesignTrigger extends SimpleCriterionTrigger<IntentionalGameDesignTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos pos) {
        final Vec3 posD = Vec3.atCenterOf(pos);
        trigger(player, instance -> instance.matches(player.serverLevel(), posD));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<LocationPredicate> respawn
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "respawn").forGetter(TriggerInstance::respawn)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> clicked(LocationPredicate respawn) {
            return BingoTriggers.INTENTIONAL_GAME_DESIGN.createCriterion(new TriggerInstance(
                Optional.empty(), Optional.ofNullable(respawn)
            ));
        }

        public boolean matches(ServerLevel level, Vec3 pos) {
            return respawn.isEmpty() || respawn.get().matches(level, pos.x, pos.y, pos.z);
        }
    }
}
