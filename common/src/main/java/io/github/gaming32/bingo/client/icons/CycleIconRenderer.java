package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class CycleIconRenderer implements AbstractCycleIconRenderer<CycleIcon> {
    public static final long TIME_PER_ICON = 2000;

    @Override
    public void renderWithParentPeriod(int parentPeriod, CycleIcon icon, GuiGraphics graphics, int x, int y) {
        final List<GoalIcon> icons = icon.icons();
        if (icons.isEmpty()) return;
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
        if (icons.isEmpty()) return;
        final GoalIcon subIcon = getIcon(icons, parentPeriod);
        IconRenderer<GoalIcon> subRenderer = IconRenderers.getRenderer(subIcon);
        if (subRenderer instanceof AbstractCycleIconRenderer<GoalIcon> subCycleRenderer) {
            subCycleRenderer.renderDecorationsWithParentPeriod(parentPeriod * icons.size(), subIcon, font, graphics, x, y);
        } else {
            subRenderer.renderDecorations(subIcon, font, graphics, x, y);
        }
    }

    private static GoalIcon getIcon(List<GoalIcon> icons, int parentPeriod) {
        return icons.get((int)((Util.getMillis() / (TIME_PER_ICON * parentPeriod)) % icons.size()));
    }
}
