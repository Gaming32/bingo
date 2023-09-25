package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
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
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player);
    }

    public void trigger(ServerPlayer player, JukeboxBlockEntity blockEntity) {
        trigger(player, instance -> instance.matches(blockEntity));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(Optional<ContextAwarePredicate> predicate) {
            super(predicate);
        }

        public static TriggerInstance partyParrots() {
            return new TriggerInstance(Optional.empty());
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
