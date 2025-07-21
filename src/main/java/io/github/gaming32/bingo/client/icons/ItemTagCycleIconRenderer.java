package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public class ItemTagCycleIconRenderer implements AbstractCycleIconRenderer<ItemTagCycleIcon> {
    @Override
    public void renderWithParentPeriod(int parentPeriod, ItemTagCycleIcon icon, GuiGraphics graphics, int x, int y) {
        graphics.renderFakeItem(getIconItemWithParentPeriod(parentPeriod, icon), x, y);
    }

    @Override
    public void renderDecorationsWithParentPeriod(int parentPeriod, ItemTagCycleIcon icon, Font font, GuiGraphics graphics, int x, int y) {
        IconRenderer.renderCount(icon.count(), font, graphics, x, y);
    }

    @Override
    public ItemStack getIconItemWithParentPeriod(int parentPeriod, ItemTagCycleIcon icon) {
        return BuiltInRegistries.ITEM.get(icon.tag())
            .map(holders -> AbstractCycleIconRenderer.getIconFromTag(holders, parentPeriod)
            .map(itemHolder -> new ItemStack(itemHolder, icon.count()))
            .orElse(ItemStack.EMPTY)).orElse(ItemStack.EMPTY);
    }
}
