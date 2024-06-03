package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.BingoTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class BingoTagProvider extends FabricCodecDataProvider<BingoTag> {
    public BingoTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, PackOutput.Target.DATA_PACK, "bingo/tags", BingoTag.CODEC);
    }

    @Override
    @NotNull
    public String getName() {
        return "Bingo tags";
    }

    @Override
    protected void configure(BiConsumer<ResourceLocation, BingoTag> adder, HolderLookup.Provider registries) {
        BingoTag.builder(BingoTags.ACTION)
            .difficultyMax(20, 20, 20, 20, 20)
            .build(adder);
        BingoTag.builder(BingoTags.BUILD)
            .difficultyMax(20, 20, 20, 20, 20)
            .build(adder);
        BingoTag.builder(BingoTags.COLOR)
            .difficultyMax(2, 2, 2, 2, 2)
            .build(adder);
        BingoTag.builder(BingoTags.COMBAT)
            .difficultyMax(5, 10, 20, 20, 20)
            .build(adder);
        BingoTag.builder(BingoTags.END)
            .difficultyMax(0, 0, 0, 1, 5)
            .build(adder);
        BingoTag.builder(BingoTags.FINISH)
            .difficultyMax(1, 1, 1, 1, 1)
            .disallowOnSameLine()
            .specialType(BingoTag.SpecialType.FINISH)
            .build(adder);
        BingoTag.builder(BingoTags.ITEM)
            .difficultyMax(25, 25, 20, 20, 20)
            .build(adder);
        BingoTag.builder(BingoTags.NETHER)
            .difficultyMax(0, 2, 5, 10, 15)
            .build(adder);
        BingoTag.builder(BingoTags.NEVER)
            .difficultyMax(3, 3, 3, 2, 1)
            .disallowOnSameLine()
            .specialType(BingoTag.SpecialType.NEVER)
            .build(adder);
        BingoTag.builder(BingoTags.OCEAN)
            .difficultyMax(5, 5, 5, 5, 5)
            .build(adder);
        BingoTag.builder(BingoTags.OVERWORLD)
            .difficultyMax(25, 25, 24, 21, 18)
            .build(adder);
        BingoTag.builder(BingoTags.RARE_BIOME)
            .difficultyMax(0, 1, 2, 4, 6)
            .build(adder);
        BingoTag.builder(BingoTags.STAT)
            .difficultyMax(5, 5, 5, 5, 5)
            .build(adder);
        BingoTag.builder(BingoTags.STALEMATE)
            .markerTag()
            .build(adder);
        BingoTag.builder(BingoTags.VILLAGE)
            .difficultyMax(0, 1, 2, 3, 4)
            .build(adder);
    }
}
