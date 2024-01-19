package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public record IndicatorIcon(GoalIcon base, GoalIcon indicator) implements GoalIcon {
    public static final Codec<IndicatorIcon> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            GoalIcon.CODEC.fieldOf("base").forGetter(IndicatorIcon::base),
            GoalIcon.CODEC.fieldOf("indicator").forGetter(IndicatorIcon::indicator)
        ).apply(instance, IndicatorIcon::new)
    );

    public static IndicatorIcon infer(Object base, Object indicator) {
        return new IndicatorIcon(GoalIcon.infer(base), GoalIcon.infer(indicator));
    }

    @Override
    public ItemStack item() {
        return base.item();
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.INDICATOR.get();
    }
}
