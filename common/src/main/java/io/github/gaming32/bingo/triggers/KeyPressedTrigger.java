package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class KeyPressedTrigger extends SimpleCriterionTrigger<KeyPressedTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String key) {
        trigger(player, instance -> instance.matches(key));
    }

    @Override
    public boolean bingo$requiresClientCode() {
        return true;
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<String> key
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.optionalFieldOf("key").forGetter(TriggerInstance::key)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> keyPressed() {
            return BingoTriggers.KEY_PRESSED.get().createCriterion(new TriggerInstance(
                Optional.empty(), Optional.empty()
            ));
        }

        public static Criterion<TriggerInstance> keyPressed(String key) {
            return BingoTriggers.KEY_PRESSED.get().createCriterion(new TriggerInstance(
                Optional.empty(), Optional.of(key)
            ));
        }

        public boolean matches(String key) {
            return this.key.isEmpty() || this.key.get().equals(key);
        }
    }
}
