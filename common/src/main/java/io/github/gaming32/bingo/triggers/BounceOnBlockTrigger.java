package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BounceOnBlockTrigger extends SimpleCriterionTrigger<BounceOnBlockTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos blockPos) {
        trigger(player, instance -> instance.matches(player.serverLevel(), blockPos));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<LocationPredicate> block
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "block").forGetter(TriggerInstance::block)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> bounceOnBlock(BlockPredicate.Builder block) {
            return BingoTriggers.BOUNCE_ON_BLOCK.get().createCriterion(new TriggerInstance(
                Optional.empty(), Optional.of(LocationPredicate.Builder.location().setBlock(block).build())
            ));
        }

        public boolean matches(ServerLevel level, BlockPos pos) {
            return block.isEmpty() || block.get().matches(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }
    }
}
