package io.github.gaming32.bingo.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.util.ResourceLocations;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.List;

public record BingoDifficulty(int number, @Nullable String fallbackName, @Nullable List<Float> distribution) {
    public static final Codec<BingoDifficulty> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("number").forGetter(BingoDifficulty::number),
            Codec.STRING.optionalFieldOf("fallback_name").forGetter(d -> Optional.ofNullable(d.fallbackName)),
            Codec.FLOAT.listOf().optionalFieldOf("distribution").forGetter(dist -> Optional.ofNullable(dist.distribution))
        ).apply(instance, BingoDifficulty::new)
    );

    private static Map<ResourceLocation, Holder> byId = Map.of();
    private static NavigableMap<Integer, Holder> byNumber = ImmutableSortedMap.of();

    private BingoDifficulty(int number, Optional<String> fallbackName, Optional<List<Float>> distribution) {
        this(number, fallbackName.orElse(null), distribution.orElse(null));
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    @Nullable
    public static Holder byId(ResourceLocation id) {
        return byId.get(id);
    }

    @Nullable
    public static Holder byNumber(int number) {
        return byNumber.get(number);
    }

    public static Set<ResourceLocation> getIds() {
        return byId.keySet();
    }

    public static NavigableSet<Integer> getNumbers() {
        return byNumber.navigableKeySet();
    }

    private static Component getDescription(ResourceLocation id, String fallback) {
        return Component.translatableWithFallback(id.toLanguageKey("bingo_difficulty"), fallback);
    }

    public record Holder(ResourceLocation id, BingoDifficulty difficulty) {
        public Component getDescription() {
            return BingoDifficulty.getDescription(id, difficulty.fallbackName);
        }

        @Override
        public String toString() {
            return id.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Holder h && id.equals(h.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    public static final class Builder {
        private final ResourceLocation id;
        private Integer number;
        private String fallbackName;
        private List<Float> distribution;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder number(int number) {
            this.number = number;
            return this;
        }

        public Builder fallbackName(String name) {
            this.fallbackName = name;
            return this;
        }

        public Builder distribution(int... scaledBy5x5) {
            final float[] unscaled = new float[scaledBy5x5.length];
            for (int i = 0; i < scaledBy5x5.length; i++) {
                unscaled[i] = scaledBy5x5[i] / 25f;
            }
            return this.distribution(unscaled);
        }

        public Builder distribution(float... unscaledDistribution) {
            this.distribution = FloatList.of(unscaledDistribution);
            return this;
        }

        public Holder build() {
            return new Holder(id, new BingoDifficulty(
                Objects.requireNonNull(number, "number"),
                fallbackName,
                distribution
            ));
        }

        public void build(BiConsumer<ResourceLocation, BingoDifficulty> adder) {
            final Holder result = build();
            adder.accept(result.id, result.difficulty);
        }
    }

    public static final class ReloadListener extends SimpleJsonResourceReloadListener {
        public static final ResourceLocation ID = ResourceLocations.bingo("difficulties");
        private static final Gson GSON = new GsonBuilder().create();

        private final HolderLookup.Provider registries;

        public ReloadListener(HolderLookup.Provider registries) {
            super(GSON, "bingo/difficulties");
            this.registries = registries;
        }

        @NotNull
        @Override
        public String getName() {
            return "Bingo Difficulties";
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
            final RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
            final ImmutableMap.Builder<ResourceLocation, Holder> byIdBuilder = ImmutableMap.builder();
            final ImmutableSortedMap.Builder<Integer, Holder> byNumberBuilder = ImmutableSortedMap.naturalOrder();
            for (final var entry : object.entrySet()) {
                try {
                    final BingoDifficulty difficulty = CODEC.parse(ops, entry.getValue()).getOrThrow(JsonParseException::new);
                    final Holder holder = new Holder(entry.getKey(), difficulty);
                    byIdBuilder.put(holder.id, holder);
                    byNumberBuilder.put(difficulty.number, holder);
                } catch (Exception e) {
                    Bingo.LOGGER.error("Parsing error in bingo difficulty {}: {}", entry.getKey(), e.getMessage());
                }
            }
            byId = byIdBuilder.build();
            byNumber = byNumberBuilder.buildOrThrow();
            Bingo.LOGGER.info("Loaded {} bingo difficulties", byId.size());
        }
    }
}
