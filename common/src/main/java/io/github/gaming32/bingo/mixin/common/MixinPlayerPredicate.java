package io.github.gaming32.bingo.mixin.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.ext.PlayerPredicateExt;
import io.github.gaming32.bingo.game.BingoGame;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Debug(export = true)
@Mixin(PlayerPredicate.class)
public abstract class MixinPlayerPredicate implements PlayerPredicateExt {
    @Shadow
    private static <T> Stat<T> getStat(StatType<T> type, ResourceLocation id) {
        throw new AssertionError();
    }

    @Shadow
    private static <T> ResourceLocation getStatValueId(Stat<T> stat) {
        throw new AssertionError();
    }

    @Unique
    private Map<Stat<?>, MinMaxBounds.Ints> bingo$relativeStats;

    @Override
    public void bingo$setRelativeStats(Map<Stat<?>, MinMaxBounds.Ints> relativeStats) {
        bingo$relativeStats = relativeStats;
    }

    @ModifyReturnValue(method = "matches", at = @At("RETURN"))
    private boolean matchesCustom(boolean original, @Local Entity entity) {
        if (!original || !(entity instanceof ServerPlayer player)) {
            return original;
        }
        if (bingo$relativeStats != null) {
            final BingoGame game = Bingo.activeGame;
            if (game != null) {
                final Object2IntMap<Stat<?>> baseStats = game.getBaseStats(player);
                if (baseStats != null) {
                    final StatsCounter currentStats = player.getStats();
                    for (final var entry : bingo$relativeStats.entrySet()) {
                        final int value = currentStats.getValue(entry.getKey()) - baseStats.getInt(entry.getKey());
                        if (!entry.getValue().matches(value)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Inject(method = "fromJson", at = @At("RETURN"))
    private static void deserializeCustomFields(JsonObject json, CallbackInfoReturnable<MixinPlayerPredicate> cir) {
        final JsonArray relativeStatsArray = GsonHelper.getAsJsonArray(json, "bingo:relative_stats", null);
        if (relativeStatsArray != null) {
            final Map<Stat<?>, MinMaxBounds.Ints> relativeStats = new HashMap<>();
            for (final JsonElement element : relativeStatsArray) {
                final JsonObject obj = GsonHelper.convertToJsonObject(element, "relative stats entry");

                final ResourceLocation typeId = new ResourceLocation(GsonHelper.getAsString(obj, "type"));
                final StatType<?> type = BuiltInRegistries.STAT_TYPE.get(typeId);
                if (type == null) {
                    throw new JsonParseException("Invalid stat type: " + typeId);
                }

                final ResourceLocation statId = new ResourceLocation(GsonHelper.getAsString(obj, "stat"));
                final Stat<?> stat = getStat(type, statId);
                final MinMaxBounds.Ints value = MinMaxBounds.Ints.fromJson(obj.get("value"));
                relativeStats.put(stat, value);
            }
            cir.getReturnValue().bingo$relativeStats = relativeStats;
        }
    }

    @Inject(method = "serializeCustomData", at = @At(value = "RETURN"))
    private void serializeCustomFields(CallbackInfoReturnable<JsonObject> cir) {
        final JsonObject result = cir.getReturnValue();

        if (bingo$relativeStats != null) {
            final JsonArray statsArray = new JsonArray(bingo$relativeStats.size());
            bingo$relativeStats.forEach((stat, value) -> {
                final JsonObject obj = new JsonObject();
                obj.addProperty("type", BuiltInRegistries.STAT_TYPE.getKey(stat.getType()).toString());
                obj.addProperty("stat", getStatValueId(stat).toString());
                obj.add("value", value.serializeToJson());
                statsArray.add(obj);
            });
            result.add("bingo:relative_stats", statsArray);
        }
    }
}
