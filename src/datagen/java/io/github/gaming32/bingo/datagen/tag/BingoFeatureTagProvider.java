package io.github.gaming32.bingo.datagen.tag;

import io.github.gaming32.bingo.data.tags.bingo.BingoFeatureTags;
import io.github.gaming32.bingo.datagen.BingoDataGenUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BingoFeatureTagProvider extends FabricTagsProvider<Feature> {
    public BingoFeatureTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.FEATURE, registriesFuture);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider arg) {
        builder(BingoFeatureTags.TREES).add(getTreeFeatures());

        builder(BingoFeatureTags.HUGE_MUSHROOMS).add(
            TreeFeatures.HUGE_BROWN_MUSHROOM,
            TreeFeatures.HUGE_RED_MUSHROOM
        );

        builder(BingoFeatureTags.HUGE_FUNGI).add(
            TreeFeatures.WARPED_FUNGUS_PLANTED,
            TreeFeatures.WARPED_FUNGUS,
            TreeFeatures.CRIMSON_FUNGUS_PLANTED,
            TreeFeatures.CRIMSON_FUNGUS
        );

        builder(BingoFeatureTags.MEGA_JUNGLE_TREES).add(
            TreeFeatures.MEGA_JUNGLE_TREE
        );
    }

    @SuppressWarnings("unchecked")
    private static ResourceKey<Feature>[] getTreeFeatures() {
        List<ResourceKey<Feature>> result = new ArrayList<>();
        for (ResourceKey<Feature> featureKey : BingoDataGenUtil.getResourceKeysFromClass(TreeFeatures.class, Registries.FEATURE)) {
            String path = featureKey.identifier().getPath();
            if (!path.contains("fungus") && !path.contains("mushroom")) {
                result.add(featureKey);
            }
        }
        return result.toArray(ResourceKey[]::new);
    }
}
