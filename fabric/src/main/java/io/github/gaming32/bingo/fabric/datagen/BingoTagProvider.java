package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.util.BingoUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BingoTagProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public BingoTagProvider(FabricDataOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "bingo/tags");
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(CachedOutput output) {
        Set<ResourceLocation> existingTags = new HashSet<>();
        List<CompletableFuture<?>> generators = new ArrayList<>();

        Consumer<BingoTag.Holder> tagAdder = tag -> {
            if (!existingTags.add(tag.id())) {
                throw new IllegalArgumentException("Duplicate tag " + tag.id());
            } else {
                Path path = pathProvider.json(tag.id());
                generators.add(DataProvider.saveStable(
                    output, BingoUtil.toJsonElement(BingoTag.CODEC, tag.tag()), path
                ));
            }
        };

        addTags(tagAdder);

        return CompletableFuture.allOf(generators.toArray(CompletableFuture[]::new));
    }

    @Override
    @NotNull
    public String getName() {
        return "Bingo tags";
    }

    private void addTags(Consumer<BingoTag.Holder> tagAdder) {
        tagAdder.accept(BingoTag.builder(BingoTags.ACTION).difficultyMax(20, 20, 20, 20, 20).build());
        tagAdder.accept(BingoTag.builder(BingoTags.BUILD).difficultyMax(20, 20, 20, 20, 20).build());
        tagAdder.accept(BingoTag.builder(BingoTags.COLOR).difficultyMax(2, 2, 2, 2, 2).build());
        tagAdder.accept(BingoTag.builder(BingoTags.COMBAT).difficultyMax(5, 10, 20, 20, 20).build());
        tagAdder.accept(BingoTag.builder(BingoTags.END).difficultyMax(0, 0, 0, 1, 5).build());
        tagAdder.accept(BingoTag.builder(BingoTags.FINISH)
            .difficultyMax(1, 1, 1, 1, 1)
            .disallowOnSameLine()
            .specialType(BingoTag.SpecialType.FINISH)
            .build()
        );
        tagAdder.accept(BingoTag.builder(BingoTags.ITEM).difficultyMax(25, 25, 20, 20, 20).build());
        tagAdder.accept(BingoTag.builder(BingoTags.NETHER).difficultyMax(0, 2, 5, 10, 15).build());
        tagAdder.accept(BingoTag.builder(BingoTags.NEVER)
            .difficultyMax(3, 3, 3, 2, 1)
            .disallowOnSameLine()
            .specialType(BingoTag.SpecialType.NEVER)
            .build()
        );
        tagAdder.accept(BingoTag.builder(BingoTags.OCEAN).difficultyMax(5, 5, 5, 5, 5).build());
        tagAdder.accept(BingoTag.builder(BingoTags.OVERWORLD).difficultyMax(25, 25, 24, 21, 18).build());
        tagAdder.accept(BingoTag.builder(BingoTags.RARE_BIOME).difficultyMax(0, 1, 2, 4, 6).build());
        tagAdder.accept(BingoTag.builder(BingoTags.STAT).difficultyMax(5, 5, 5, 5, 5).build());
        tagAdder.accept(BingoTag.builder(BingoTags.VILLAGE).difficultyMax(0, 1, 2, 3, 4).build());
    }
}
