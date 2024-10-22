package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

public record IndicatorIcon(GoalIcon base, GoalIcon indicator) implements GoalIcon {
    public static final MapCodec<IndicatorIcon> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            GoalIcon.CODEC.fieldOf("base").forGetter(IndicatorIcon::base),
            GoalIcon.CODEC.fieldOf("indicator").forGetter(IndicatorIcon::indicator)
        ).apply(instance, IndicatorIcon::new)
    );

    public static IndicatorIcon infer(Object base, Object indicator) {
        return new IndicatorIcon(GoalIcon.infer(base), GoalIcon.infer(indicator));
    }

    @Override
    public ItemStack getFallback(RegistryAccess registries) {
        return base.getFallback(registries);
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.INDICATOR.get();
    }
}
