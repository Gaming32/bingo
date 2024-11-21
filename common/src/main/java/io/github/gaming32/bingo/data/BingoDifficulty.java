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

import java.util.NavigableSet;

public record BingoDifficulty(Component description, int number) {
    public static final Codec<BingoDifficulty> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ComponentSerialization.CODEC.fieldOf("description").forGetter(BingoDifficulty::description),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("number").forGetter(BingoDifficulty::number)
        ).apply(instance, BingoDifficulty::new)
    );

    public static NavigableSet<Integer> getNumbers(HolderLookup<BingoDifficulty> lookup) {
        return lookup.listElements()
            .map(Holder.Reference::value)
            .map(BingoDifficulty::number)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
    }
}
