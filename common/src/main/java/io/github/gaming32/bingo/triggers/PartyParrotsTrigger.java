package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class PartyParrotsTrigger extends SimpleCriterionTrigger<PartyParrotsTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, JukeboxBlockEntity blockEntity) {
        trigger(player, instance -> instance.matches(blockEntity));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> partyParrots() {
            return BingoTriggers.PARTY_PARROTS.get().createCriterion(
                new TriggerInstance(Optional.empty())
            );
        }

        public boolean matches(JukeboxBlockEntity blockEntity) {
            if (!blockEntity.isRecordPlaying() || blockEntity.getLevel() == null) {
                return false;
            }
            final Set<Parrot.Variant> parrotsNeeded = EnumSet.allOf(Parrot.Variant.class);
            for (final Parrot parrot : blockEntity.getLevel().getEntitiesOfClass(
                Parrot.class, new AABB(blockEntity.getBlockPos()).inflate(3.0)
            )) {
                parrotsNeeded.remove(parrot.getVariant());
                if (parrotsNeeded.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }
}
