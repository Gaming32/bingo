package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CycleIconRenderer implements AbstractCycleIconRenderer<CycleIcon> {
    @Override
    public void renderWithParentPeriod(int parentPeriod, CycleIcon icon, GuiGraphics graphics, int x, int y) {
        final List<GoalIcon> icons = icon.icons();
        final GoalIcon subIcon = getIcon(icons, parentPeriod);
        IconRenderer<GoalIcon> subRenderer = IconRenderers.getRenderer(subIcon);
        if (subRenderer instanceof AbstractCycleIconRenderer<GoalIcon> subCycleRenderer) {
            subCycleRenderer.renderWithParentPeriod(parentPeriod * icons.size(), subIcon, graphics, x, y);
        } else {
            subRenderer.render(subIcon, graphics, x, y);
        }
    }

    @Override
    public void renderDecorationsWithParentPeriod(int parentPeriod, CycleIcon icon, Font font, GuiGraphics graphics, int x, int y) {
        final List<GoalIcon> icons = icon.icons();
        final GoalIcon subIcon = getIcon(icons, parentPeriod);
        IconRenderer<GoalIcon> subRenderer = IconRenderers.getRenderer(subIcon);
        if (subRenderer instanceof AbstractCycleIconRenderer<GoalIcon> subCycleRenderer) {
            subCycleRenderer.renderDecorationsWithParentPeriod(parentPeriod * icons.size(), subIcon, font, graphics, x, y);
        } else {
            subRenderer.renderDecorations(subIcon, font, graphics, x, y);
        }
    }

    @Override
    public ItemStack getIconItemWithParentPeriod(int parentPeriod, CycleIcon icon) {
        final List<GoalIcon> icons = icon.icons();
        final GoalIcon subIcon = getIcon(icons, parentPeriod);
        IconRenderer<GoalIcon> subRenderer = IconRenderers.getRenderer(subIcon);
        if (subRenderer instanceof AbstractCycleIconRenderer<GoalIcon> subCycleRenderer) {
            return subCycleRenderer.getIconItemWithParentPeriod(parentPeriod * icons.size(), subIcon);
        } else {
            return subRenderer.getIconItem(subIcon);
        }
    }

    private static GoalIcon getIcon(List<GoalIcon> icons, int parentPeriod) {
        return AbstractCycleIconRenderer.getIcon(icons::get, icons.size(), parentPeriod);
    }
}
