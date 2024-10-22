package io.github.gaming32.bingo.data;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;

public record BingoDifficulty(int number, @Nullable String fallbackName) {
    public static final Codec<BingoDifficulty> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("number").forGetter(BingoDifficulty::number),
            Codec.STRING.optionalFieldOf("fallback_name").forGetter(d -> Optional.ofNullable(d.fallbackName))
        ).apply(instance, BingoDifficulty::new)
    );
    public static final Codec<BingoDifficulty> NETWORK_CODEC = Codec.unit(new BingoDifficulty(0, Optional.empty()));

    private BingoDifficulty(int number, Optional<String> fallbackName) {
        this(number, fallbackName.orElse(null));
    }

    public static NavigableSet<Integer> getNumbers(HolderLookup<BingoDifficulty> lookup) {
        return lookup.listElements()
            .map(Holder.Reference::value)
            .map(BingoDifficulty::number)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Component getDescription(Holder<BingoDifficulty> holder) {
        return getDescription(holder.unwrapKey().orElse(null));
    }

    public static Component getDescription(@Nullable ResourceKey<BingoDifficulty> id) {
        return getDescription((ResourceLocation)Optionull.map(id, ResourceKey::location));
    }

    public static Component getDescription(@Nullable ResourceLocation id) {
        return Component.translatable(Util.makeDescriptionId("bingo_difficulty", id));
    }

    public static final class Builder {
        private Integer number;
        private String fallbackName;

        private Builder() {
        }

        public Builder number(int number) {
            this.number = number;
            return this;
        }

        public Builder fallbackName(String name) {
            this.fallbackName = name;
            return this;
        }

        public BingoDifficulty build() {
            return new BingoDifficulty(
                Objects.requireNonNull(number, "number"),
                fallbackName
            );
        }
    }
}
