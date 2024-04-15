package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ItemTagCycleIconRenderer implements AbstractCycleIconRenderer<ItemTagCycleIcon> {
    @Override
    public void renderWithParentPeriod(int parentPeriod, ItemTagCycleIcon icon, GuiGraphics graphics, int x, int y) {
        final Optional<HolderSet.Named<Item>> items = BuiltInRegistries.ITEM.getTag(icon.tag());
        if (items.isEmpty() || items.get().size() == 0) return;
        graphics.renderFakeItem(new ItemStack(getIcon(items.get(), parentPeriod)), x, y);
    }

    @Override
    public void renderDecorationsWithParentPeriod(int parentPeriod, ItemTagCycleIcon icon, Font font, GuiGraphics graphics, int x, int y) {
        IconRenderer.renderCount(icon.count(), font, graphics, x, y);
    }

    @Override
    public ItemStack getIconItemWithParentPeriod(int parentPeriod, ItemTagCycleIcon icon) {
        final Optional<HolderSet.Named<Item>> items = BuiltInRegistries.ITEM.getTag(icon.tag());
        if (items.isEmpty() || items.get().size() == 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(getIcon(items.get(), parentPeriod), icon.count());
    }

    private static Holder<Item> getIcon(HolderSet.Named<Item> icons, int parentPeriod) {
        return icons.get((int)((Util.getMillis() / (CycleIconRenderer.TIME_PER_ICON * parentPeriod)) % icons.size()));
    }
}
