package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class KeyPressedTrigger extends SimpleCriterionTrigger<KeyPressedTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            json.has("key")
                ? Optional.of(GsonHelper.getAsString(json, "key"))
                : Optional.empty()
        );
    }

    public void trigger(ServerPlayer player, String key) {
        trigger(player, instance -> instance.matches(key));
    }

    @Override
    public boolean requiresClientCode() {
        return true;
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<String> key;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> key) {
            super(player);
            this.key = key;
        }

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

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            key.ifPresent(p -> result.addProperty("key", p));
            return result;
        }

        public boolean matches(String key) {
            return this.key.isEmpty() || this.key.get().equals(key);
        }
    }
}
