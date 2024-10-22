package io.github.gaming32.bingo.fabric.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.gaming32.bingo.data.BingoDifficulties;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.fabric.datagen.goal.BingoGoalProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoBlockTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoDamageTypeTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoEntityTypeTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoFeatureTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoItemTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.JsonKeySortOrderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class BingoDataGenFabric implements DataGeneratorEntrypoint {
    private static final boolean DUMP_BINGO_COMMAND = false;

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(BingoGoalProvider::new);
        pack.addProvider(BingoRegistriesDatapackGenerator::new);

        final BingoBlockTagProvider blockTagProvider = pack.addProvider(BingoBlockTagProvider::new);
        pack.addProvider((output, registriesFuture) -> new BingoItemTagProvider(output, registriesFuture, blockTagProvider));
        pack.addProvider(BingoEntityTypeTagProvider::new);
        pack.addProvider(BingoFeatureTagProvider::new);
        pack.addProvider(BingoDamageTypeTagProvider::new);

        if (DUMP_BINGO_COMMAND) {
            pack.addProvider(BingoCommandDumper::new);
        }
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(BingoRegistries.TAG, BingoTags::bootstrap);
        registryBuilder.add(BingoRegistries.DIFFICULTY, BingoDifficulties::bootstrap);
    }

    @Override
    public void addJsonKeySortOrders(JsonKeySortOrderCallback callback) {
        callback.add("bingo_type", 0);
    }

    public static <T> Set<T> loadVanillaTag(TagKey<T> tag, HolderLookup.Provider registries) {
        final var registry = registries.lookupOrThrow(tag.registry());
        final IoSupplier<InputStream> resource = Minecraft.getInstance()
            .getVanillaPackResources()
            .getResource(
                PackType.SERVER_DATA, ResourceLocation.fromNamespaceAndPath(
                    tag.location().getNamespace(),
                    Registries.tagsDirPath(tag.registry()) + '/' + tag.location().getPath() + ".json"
                )
            );
        if (resource == null) {
            throw new IllegalArgumentException("Unknown tag " + tag);
        }
        try (Reader input = new InputStreamReader(resource.get())) {
            final JsonElement json = JsonParser.parseReader(input);
            final TagFile file = TagFile.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json)
                .getOrThrow(JsonParseException::new);
            return file.entries()
                .stream()
                .<T>mapMulti((entry, out) -> entry.build(
                    new TagEntry.Lookup<>() {
                        @Nullable
                        @Override
                        public T element(ResourceLocation elementLocation, boolean readOnly) {
                            return registry.get(ResourceKey.create(tag.registry(), elementLocation))
                                .map(Holder.Reference::value)
                                .orElse(null);
                        }

                        @Nullable
                        @Override
                        public Collection<T> tag(ResourceLocation tagLocation) {
                            return loadVanillaTag(TagKey.create(tag.registry(), tagLocation), registries);
                        }
                    }, out
                ))
                .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
