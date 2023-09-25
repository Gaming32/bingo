package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.LocationPredicateExt;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocationPredicate.class)
public class MixinLocationPredicate implements LocationPredicateExt {
    @Unique
    @Nullable
    private TagPredicate<LevelStem> bingo$dimensionTag;

    @Unique
    @Nullable
    private TagPredicate<Biome> bingo$biomeTag;

    @Override
    public void bingo$setDimensionTag(TagPredicate<LevelStem> dimensionTag) {
        bingo$dimensionTag = dimensionTag;
    }

    @Override
    public void bingo$setBiomeTag(TagPredicate<Biome> biomeTag) {
        bingo$biomeTag = biomeTag;
    }

    // TODO: Rework
//    @ModifyReturnValue(method = "serializeToJson", at = @At("RETURN"))
//    private JsonElement serializeToJsonExt(JsonElement jsonElt) {
//        if (jsonElt instanceof JsonObject json) {
//            if (bingo$dimensionTag != null) {
//                json.add("bingo:dimension_tag", bingo$dimensionTag.serializeToJson());
//            }
//            if (bingo$biomeTag != null) {
//                json.add("bingo:biome_tag", bingo$biomeTag.serializeToJson());
//            }
//        }
//        return jsonElt;
//    }
//
//    @ModifyReturnValue(method = "fromJson", at = @At("RETURN"))
//    private static LocationPredicate fromJsonExt(LocationPredicate predicate, JsonElement jsonElt) {
//        if (predicate == LocationPredicate.ANY) {
//            return predicate;
//        }
//        JsonObject json = jsonElt.getAsJsonObject();
//        if (json.has("bingo:dimension_tag")) {
//            ((LocationPredicateExt) predicate).bingo$setDimensionTag(TagPredicate.fromJson(json.get("bingo:dimension_tag"), Registries.LEVEL_STEM));
//        }
//        if (json.has("bingo:biome_tag")) {
//            ((LocationPredicateExt) predicate).bingo$setBiomeTag(TagPredicate.fromJson(json.get("bingo:biome_tag"), Registries.BIOME));
//        }
//        return predicate;
//    }

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void matchesExt(ServerLevel level, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (bingo$dimensionTag != null) {
            ResourceKey<LevelStem> dimensionKey = Registries.levelToLevelStem(level.dimension());
            Holder<LevelStem> dimensionHolder = level.registryAccess().registryOrThrow(Registries.LEVEL_STEM).getHolderOrThrow(dimensionKey);
            if (!bingo$dimensionTag.matches(dimensionHolder)) {
                cir.setReturnValue(false);
                return;
            }
        }
        if (bingo$biomeTag != null) {
            if (!bingo$biomeTag.matches(level.getBiome(BlockPos.containing(x, y, z)))) {
                cir.setReturnValue(false);
            }
        }
    }
}
