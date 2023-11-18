package io.github.gaming32.bingo.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record BingoDifficulty(int number, @Nullable String fallbackName) {
    public static final Codec<BingoDifficulty> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("number").forGetter(BingoDifficulty::number),
            ExtraCodecs.strictOptionalField(Codec.STRING, "fallback_name").forGetter(d -> Optional.ofNullable(d.fallbackName))
        ).apply(instance, BingoDifficulty::new)
    );

    private static Map<ResourceLocation, Holder> byId = Map.of();
    private static NavigableMap<Integer, Holder> byNumber = ImmutableSortedMap.of();

    private BingoDifficulty(int number, Optional<String> fallbackName) {
        this(number, fallbackName.orElse(null));
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

        public Holder build() {
            return new Holder(id, new BingoDifficulty(
                Objects.requireNonNull(number, "number"),
                fallbackName
            ));
        }
    }

    public static final class ReloadListener extends SimpleJsonResourceReloadListener {
        public static final ResourceLocation ID = new ResourceLocation("bingo:difficulties");
        private static final Gson GSON = new GsonBuilder().create();

        public ReloadListener() {
            super(GSON, "bingo/difficulties");
        }

        @NotNull
        @Override
        public String getName() {
            return "Bingo Difficulties";
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
            final ImmutableMap.Builder<ResourceLocation, Holder> byIdBuilder = ImmutableMap.builder();
            final ImmutableSortedMap.Builder<Integer, Holder> byNumberBuilder = ImmutableSortedMap.naturalOrder();
            for (final var entry : object.entrySet()) {
                try {
                    final BingoDifficulty difficulty = BingoUtil.fromJsonElement(CODEC, entry.getValue());
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
