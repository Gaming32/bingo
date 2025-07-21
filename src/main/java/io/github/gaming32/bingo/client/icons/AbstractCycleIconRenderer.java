package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface AbstractCycleIconRenderer<I extends GoalIcon> extends IconRenderer<I> {
    long TIME_PER_ICON = 2000;

    void renderWithParentPeriod(int parentPeriod, I icon, GuiGraphics graphics, int x, int y);

    @Override
    default void render(I icon, GuiGraphics graphics, int x, int y) {
        renderWithParentPeriod(1, icon, graphics, x, y);
    }

    void renderDecorationsWithParentPeriod(int parentPeriod, I icon, Font font, GuiGraphics graphics, int x, int y);

    @Override
    default void renderDecorations(I icon, Font font, GuiGraphics graphics, int x, int y) {
        renderDecorationsWithParentPeriod(1, icon, font, graphics, x, y);
    }

    @Override
    default ItemStack getIconItem(I icon) {
        return getIconItemWithParentPeriod(1, icon);
    }

    ItemStack getIconItemWithParentPeriod(int parentPeriod, I icon);

    static <T extends FeatureElement> Optional<Holder<T>> getIconFromTag(HolderSet<T> tag, int parentPeriod) {
        if (tag.size() == 0) {
            return Optional.empty();
        }
        final var level = Minecraft.getInstance().level;
        if (level == null) {
            return Optional.of(tag.get(getIconIndex(tag.size(), parentPeriod)));
        }
        final var features = level.enabledFeatures();
        final var size = (int)tag.stream()
            .filter(t -> t.value().isEnabled(features))
            .count();
        final var index = getIconIndex(size, parentPeriod);
        if (size == tag.size()) {
            return Optional.of(tag.get(index));
        }
        var stream = tag.stream().filter(t -> t.value().isEnabled(features));
        if (index > 0) {
            stream = stream.skip(index - 1);
        }
        return stream.findFirst();
    }

    static int getIconIndex(int size, int parentPeriod) {
        return (int)((Util.getMillis() / (TIME_PER_ICON * parentPeriod)) % size);
    }
}
