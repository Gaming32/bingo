package io.github.gaming32.bingo.data.icons;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public record CycleIcon(List<GoalIcon> icons) implements GoalIcon {
    public static final MapCodec<CycleIcon> CODEC = ExtraCodecs.nonEmptyList(GoalIcon.CODEC.listOf())
        .fieldOf("icons")
        .xmap(CycleIcon::new, CycleIcon::icons);
    public static final StreamCodec<RegistryFriendlyByteBuf, CycleIcon> STREAM_CODEC =
        GoalIcon.STREAM_CODEC.apply(ByteBufCodecs.list()).map(CycleIcon::new, CycleIcon::icons);

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
    public ItemStack getFallback(RegistryAccess registries) {
        return icons.getLast().getFallback(registries);
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.CYCLE.get();
    }
}
