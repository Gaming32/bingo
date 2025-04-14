package io.github.gaming32.bingo.data;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;

public record BingoDifficulty(Component description, int number, @Nullable List<Float> distribution) {
    public static final Codec<BingoDifficulty> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ComponentSerialization.CODEC.fieldOf("description").forGetter(BingoDifficulty::description),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("number").forGetter(BingoDifficulty::number),
            Codec.FLOAT.listOf().optionalFieldOf("distribution").forGetter(dist -> Optional.ofNullable(dist.distribution))
        ).apply(instance, BingoDifficulty::new)
    );

    private BingoDifficulty(Component description, int number, Optional<List<Float>> distribution) {
        this(description, number, distribution.orElse(null));
    }

    public BingoDifficulty(Component description, int number) {
        this(description, number, Optional.empty());
    }

    private static List<Float> unscale5x5(int[] scaledBy5x5) {
        final Float[] unscaled = new Float[scaledBy5x5.length];
        for (int i = 0; i < scaledBy5x5.length; i++) {
            unscaled[i] = scaledBy5x5[i] / 25f;
        }
        return Arrays.asList(unscaled);
    }

    public BingoDifficulty(Component description, int number, int[] scaledBy5x5) {
        this(description, number, unscale5x5(scaledBy5x5));
    }

    public static NavigableSet<Integer> getNumbers(HolderLookup<BingoDifficulty> lookup) {
        return lookup.listElements()
            .map(Holder.Reference::value)
            .map(BingoDifficulty::number)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
    }
}
