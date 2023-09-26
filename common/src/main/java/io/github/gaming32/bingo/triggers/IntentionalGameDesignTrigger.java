package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class IntentionalGameDesignTrigger extends SimpleCriterionTrigger<IntentionalGameDesignTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, LocationPredicate.fromJson(json.get("respawn")));
    }

    public void trigger(ServerPlayer player, BlockPos pos) {
        final Vec3 posD = Vec3.atCenterOf(pos);
        trigger(player, instance -> instance.matches(player.serverLevel(), posD));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<LocationPredicate> respawn;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> respawn) {
            super(player);
            this.respawn = respawn;
        }

        public static Criterion<TriggerInstance> clicked(LocationPredicate respawn) {
            return BingoTriggers.INTENTIONAL_GAME_DESIGN.createCriterion(new TriggerInstance(
                Optional.empty(), Optional.ofNullable(respawn)
            ));
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            respawn.ifPresent(p -> result.add("respawn", p.serializeToJson()));
            return result;
        }

        public boolean matches(ServerLevel level, Vec3 pos) {
            return respawn.isEmpty() || respawn.get().matches(level, pos.x, pos.y, pos.z);
        }
    }
}
