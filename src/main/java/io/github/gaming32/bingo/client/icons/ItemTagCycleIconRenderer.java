package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

public class ItemTagCycleIconRenderer implements AbstractCycleIconRenderer<ItemTagCycleIcon> {
    @Override
    public void renderWithParentPeriod(int parentPeriod, ItemTagCycleIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        ItemStackTemplate itemIcon = getIconItemTemplateWithParentPeriod(parentPeriod, icon);
        graphics.fakeItem(itemIcon.withCount(1).create(), x, y);
    }

    @Override
    public void renderDecorationsWithParentPeriod(int parentPeriod, ItemTagCycleIcon icon, Font font, GuiGraphicsExtractor graphics, int x, int y) {
        IconRenderer.renderCount(icon.count(), font, graphics, x, y);
    }

    @Override
    public ItemStackTemplate getIconItemTemplateWithParentPeriod(int parentPeriod, ItemTagCycleIcon icon) {
        return BuiltInRegistries.ITEM.get(icon.tag())
            .flatMap(holders -> AbstractCycleIconRenderer.getIconFromTag(holders, parentPeriod))
            .map(itemHolder -> new ItemStackTemplate(itemHolder, icon.count(), DataComponentPatch.EMPTY))
            .orElseGet(() -> new ItemStackTemplate(Items.STONE));
    }
}
