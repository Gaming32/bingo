package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class CycleIconRenderer implements IconRenderer<CycleIcon> {
    public static final long TIME_PER_ICON = 2000;

    @Override
    public void render(CycleIcon icon, GuiGraphics graphics, int x, int y) {
        final List<GoalIcon> icons = icon.icons();
        if (icons.isEmpty()) return;
        final GoalIcon subIcon = getIcon(icons);
        IconRenderers.getRenderer(subIcon).render(subIcon, graphics, x, y);
    }

    @Override
    public void renderDecorations(CycleIcon icon, Font font, GuiGraphics graphics, int x, int y) {
        final List<GoalIcon> icons = icon.icons();
        if (icons.isEmpty()) return;
        final GoalIcon subIcon = getIcon(icons);
        IconRenderers.getRenderer(subIcon).renderDecorations(subIcon, font, graphics, x, y);
    }

    private static GoalIcon getIcon(List<GoalIcon> icons) {
        return icons.get((int)((Util.getMillis() / TIME_PER_ICON) % icons.size()));
    }
}
