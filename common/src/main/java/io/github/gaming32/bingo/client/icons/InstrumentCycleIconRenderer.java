package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.InstrumentCycleIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;

public class InstrumentCycleIconRenderer implements AbstractCycleIconRenderer<InstrumentCycleIcon> {
    @Override
    public void renderWithParentPeriod(int parentPeriod, InstrumentCycleIcon icon, GuiGraphics graphics, int x, int y) {
        final var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        final var instruments = connection.registryAccess().lookupOrThrow(Registries.INSTRUMENT);
        if (instruments.size() == 0) return;
        graphics.renderFakeItem(InstrumentItem.create(icon.instrumentItem().value(), getIcon(instruments, parentPeriod)), x, y);
    }

    @Override
    public void renderDecorationsWithParentPeriod(int parentPeriod, InstrumentCycleIcon icon, Font font, GuiGraphics graphics, int x, int y) {
        var count = 1;
        if (icon.overrideCount().isPresent()) {
            count = icon.overrideCount().getAsInt();
        } else {
            final var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                count = connection.registryAccess().lookupOrThrow(Registries.INSTRUMENT).size();
            }
        }
        IconRenderer.renderCount(count, font, graphics, x, y);
    }

    @Override
    public ItemStack getIconItemWithParentPeriod(int parentPeriod, InstrumentCycleIcon icon) {
        final var connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return ItemStack.EMPTY;
        }
        final var instruments = connection.registryAccess().lookupOrThrow(Registries.INSTRUMENT);
        if (instruments.size() == 0) {
            return ItemStack.EMPTY;
        }
        return InstrumentItem.create(icon.instrumentItem().value(), getIcon(instruments, parentPeriod));
    }

    private static Holder<Instrument> getIcon(Registry<Instrument> icons, int parentPeriod) {
        return AbstractCycleIconRenderer.getIcon(icons::get, icons.size(), parentPeriod)
            .or(icons::getAny)
            .orElse(null);
    }
}
