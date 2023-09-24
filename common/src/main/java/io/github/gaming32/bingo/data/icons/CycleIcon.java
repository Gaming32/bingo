package io.github.gaming32.bingo.data.icons;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CycleIcon(List<GoalIcon> icons) implements GoalIcon {
    public static final Codec<CycleIcon> CODEC = GoalIcon.CODEC.listOf().xmap(CycleIcon::new, CycleIcon::icons);

    private static final long TIME_PER_ICON = 2000;

    public CycleIcon {
        icons = ImmutableList.copyOf(icons);
    }

    public CycleIcon(GoalIcon... icons) {
        this(ImmutableList.copyOf(icons));
    }

    @Override
    public ItemStack item() {
        return !icons.isEmpty() ? icons.get(icons.size() - 1).item() : ItemStack.EMPTY;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        if (icons.isEmpty()) return;
        getIcon().render(graphics, x, y);
    }

    @Override
    public void renderDecorations(Font font, GuiGraphics graphics, int x, int y) {
        if (icons.isEmpty()) return;
        getIcon().renderDecorations(font, graphics, x, y);
    }

    @Environment(EnvType.CLIENT)
    private GoalIcon getIcon() {
        return icons.get((int)((Util.getMillis() / TIME_PER_ICON) % icons.size()));
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.CYCLE.get();
    }
}
