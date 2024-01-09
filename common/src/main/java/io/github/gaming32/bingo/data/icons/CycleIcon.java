package io.github.gaming32.bingo.data.icons;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public record CycleIcon(List<GoalIcon> icons) implements GoalIcon {
    public static final Codec<CycleIcon> CODEC = GoalIcon.CODEC.listOf().xmap(CycleIcon::new, CycleIcon::icons);

    public CycleIcon {
        icons = ImmutableList.copyOf(icons);
    }

    public CycleIcon(GoalIcon... icons) {
        this(ImmutableList.copyOf(icons));
    }

    public static CycleIcon infer(Object... icons) {
        return new CycleIcon(Arrays.stream(icons).map(GoalIcon::infer).collect(ImmutableList.toImmutableList()));
    }

    @Override
    public ItemStack item() {
        return !icons.isEmpty() ? icons.get(icons.size() - 1).item() : ItemStack.EMPTY;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.CYCLE.get();
    }
}
