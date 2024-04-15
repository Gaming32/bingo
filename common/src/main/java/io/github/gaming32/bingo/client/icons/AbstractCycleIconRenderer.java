package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public interface AbstractCycleIconRenderer<I extends GoalIcon> extends IconRenderer<I> {
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
}
