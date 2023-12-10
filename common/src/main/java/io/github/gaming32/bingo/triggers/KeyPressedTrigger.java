package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
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
    public boolean requiresClientCode() {
        return true;
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<String> key
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(Codec.STRING, "key").forGetter(TriggerInstance::key)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> keyPressed() {
            return BingoTriggers.KEY_PRESSED.createCriterion(new TriggerInstance(
                Optional.empty(), Optional.empty()
            ));
        }

        public static Criterion<TriggerInstance> keyPressed(String key) {
            return BingoTriggers.KEY_PRESSED.createCriterion(new TriggerInstance(
                Optional.empty(), Optional.of(key)
            ));
        }

        @Environment(EnvType.CLIENT)
        public static Criterion<TriggerInstance> keyPressed(KeyMapping key) {
            return keyPressed(key.getName());
        }

        public boolean matches(String key) {
            return this.key.isEmpty() || this.key.get().equals(key);
        }
    }
}
