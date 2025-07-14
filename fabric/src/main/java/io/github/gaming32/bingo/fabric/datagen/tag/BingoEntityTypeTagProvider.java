package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.bingo.BingoEntityTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.concurrent.CompletableFuture;

public class BingoEntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider {
    public BingoEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        final var entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);

        // find passive mobs
        final FabricTagBuilder passiveBuilder = getOrCreateTagBuilder(BingoEntityTypeTags.PASSIVE);
        final FabricTagBuilder hostileBuilder = getOrCreateTagBuilder(BingoEntityTypeTags.HOSTILE);
        entityTypes.listElements().forEach(type -> {
            if (isPassive(type.value())) {
                passiveBuilder.add(type.key());
            } else if (!type.value().getCategory().isFriendly()) {
                hostileBuilder.add(type.key());
            }
        });
    }

    public static boolean isPassive(EntityType<?> entityType) {
        if (entityType == EntityType.VILLAGER) {
            return true;
        }
        return entityType.getCategory() != MobCategory.MISC && entityType.getCategory().isFriendly();
    }
}
