package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class IntentionalGameDesignTrigger extends SimpleCriterionTrigger<IntentionalGameDesignTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:intentional_game_design");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, LocationPredicate.fromJson(json.get("respawn")));
    }

    public void trigger(ServerPlayer player, BlockPos pos) {
        final Vec3 posD = Vec3.atCenterOf(pos);
        trigger(player, instance -> instance.matches(player.serverLevel(), posD));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate respawn;

        public TriggerInstance(ContextAwarePredicate player, LocationPredicate respawn) {
            super(ID, player);
            this.respawn = respawn;
        }

        public static TriggerInstance clicked(LocationPredicate respawn) {
            return new TriggerInstance(ContextAwarePredicate.ANY, respawn);
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("respawn", respawn.serializeToJson());
            return result;
        }

        public boolean matches(ServerLevel level, Vec3 pos) {
            return respawn.matches(level, pos.x, pos.y, pos.z);
        }
    }
}
