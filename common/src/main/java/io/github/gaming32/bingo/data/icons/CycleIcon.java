package io.github.gaming32.bingo.data.icons;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public record CycleIcon(List<GoalIcon> icons) implements GoalIcon {
    public static final MapCodec<CycleIcon> CODEC = GoalIcon.CODEC
        .listOf()
        .xmap(CycleIcon::new, CycleIcon::icons)
        .fieldOf("icons");

    public CycleIcon {
        icons = ImmutableList.copyOf(icons);
    }

    public CycleIcon(GoalIcon... icons) {
        this(ImmutableList.copyOf(icons));
    }

    public static CycleIcon infer(Object... icons) {
        return infer(Arrays.stream(icons));
    }

    public static CycleIcon infer(Collection<?> icons) {
        return infer(icons.stream());
    }

    public static CycleIcon infer(Iterable<?> icons) {
        return infer(StreamSupport.stream(icons.spliterator(), false));
    }

    public static CycleIcon infer(Stream<?> icons) {
        return new CycleIcon(icons.map(GoalIcon::infer).collect(ImmutableList.toImmutableList()));
    }

    @Override
    public ItemStack item() {
        return !icons.isEmpty() ? icons.getLast().item() : ItemStack.EMPTY;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.CYCLE.get();
    }
}
