package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.data.BingoItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class BingoItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public BingoItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        getOrCreateTagBuilder(BingoItemTags.ALLOWED_HEADS).add(
            Items.SKELETON_SKULL,
            Items.PLAYER_HEAD,
            Items.ZOMBIE_HEAD,
            Items.CREEPER_HEAD,
            Items.DRAGON_HEAD,
            Items.PIGLIN_HEAD
        );

        getOrCreateTagBuilder(BingoItemTags.ARMOR_HELMETS).addOptionalTag(new ResourceLocation("forge", "armors/helmets")).add(
            Items.LEATHER_HELMET,
            Items.TURTLE_HELMET,
            Items.CHAINMAIL_HELMET,
            Items.IRON_HELMET,
            Items.GOLDEN_HELMET,
            Items.DIAMOND_HELMET,
            Items.NETHERITE_HELMET
        );
        getOrCreateTagBuilder(BingoItemTags.ARMOR_CHESTPLATES).addOptionalTag(new ResourceLocation("forge", "armors/chestplates")).add(
            Items.LEATHER_CHESTPLATE,
            Items.CHAINMAIL_CHESTPLATE,
            Items.IRON_CHESTPLATE,
            Items.GOLDEN_CHESTPLATE,
            Items.DIAMOND_CHESTPLATE,
            Items.NETHERITE_CHESTPLATE
        );
        getOrCreateTagBuilder(BingoItemTags.ARMOR_LEGGINGS).addOptionalTag(new ResourceLocation("forge", "armors/leggings")).add(
            Items.LEATHER_LEGGINGS,
            Items.CHAINMAIL_LEGGINGS,
            Items.IRON_LEGGINGS,
            Items.GOLDEN_LEGGINGS,
            Items.DIAMOND_LEGGINGS,
            Items.NETHERITE_LEGGINGS
        );
        getOrCreateTagBuilder(BingoItemTags.ARMOR_BOOTS).addOptionalTag(new ResourceLocation("forge", "armors/boots")).add(
            Items.LEATHER_BOOTS,
            Items.CHAINMAIL_BOOTS,
            Items.IRON_BOOTS,
            Items.GOLDEN_BOOTS,
            Items.DIAMOND_BOOTS,
            Items.NETHERITE_BOOTS
        );
        getOrCreateTagBuilder(BingoItemTags.ARMOR)
            .addOptionalTag(new ResourceLocation("forge", "armors"))
            .addTag(BingoItemTags.ARMOR_HELMETS)
            .addTag(BingoItemTags.ARMOR_CHESTPLATES)
            .addTag(BingoItemTags.ARMOR_LEGGINGS)
            .addTag(BingoItemTags.ARMOR_BOOTS);

        getOrCreateTagBuilder(BingoItemTags.FISH_BUCKETS).add(
            Items.COD_BUCKET,
            Items.PUFFERFISH_BUCKET,
            Items.SALMON_BUCKET,
            Items.TROPICAL_FISH_BUCKET
        );
        getOrCreateTagBuilder(BingoItemTags.FISHING_JUNK).add(
            Items.LILY_PAD,
            Items.BOWL,
            Items.LEATHER,
            Items.LEATHER_BOOTS,
            Items.ROTTEN_FLESH,
            Items.STICK,
            Items.POTION,
            Items.BONE,
            Items.INK_SAC,
            Items.TRIPWIRE_HOOK
        );
        getOrCreateTagBuilder(BingoItemTags.FISHING_TREASURE).add(
            Items.BOW,
            Items.ENCHANTED_BOOK,
            Items.NAME_TAG,
            Items.NAUTILUS_SHELL,
            Items.SADDLE
        );

        getOrCreateTagBuilder(BingoItemTags.SHIELDS)
            .addOptionalTag(new ResourceLocation("forge", "shields"))
            .add(Items.SHIELD);
    }
}
