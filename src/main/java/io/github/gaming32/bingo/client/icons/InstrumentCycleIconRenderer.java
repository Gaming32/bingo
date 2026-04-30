package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.InstrumentCycleIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.InstrumentComponent;

public class InstrumentCycleIconRenderer implements AbstractCycleIconRenderer<InstrumentCycleIcon> {
    @Override
    public void renderWithParentPeriod(int parentPeriod, InstrumentCycleIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        final var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        final var instruments = connection.registryAccess().lookupOrThrow(Registries.INSTRUMENT);
        if (instruments.size() == 0) return;
        graphics.fakeItem(InstrumentItem.create(icon.instrumentItem().value(), getIcon(instruments, parentPeriod)), x, y);
    }

    @Override
    public void renderDecorationsWithParentPeriod(int parentPeriod, InstrumentCycleIcon icon, Font font, GuiGraphicsExtractor graphics, int x, int y) {
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
    public ItemStackTemplate getIconItemTemplateWithParentPeriod(int parentPeriod, InstrumentCycleIcon icon) {
        final var connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return new ItemStackTemplate(Items.STONE);
        }
        final var instruments = connection.registryAccess().lookupOrThrow(Registries.INSTRUMENT);
        if (instruments.size() == 0) {
            return new ItemStackTemplate(Items.STONE);
        }
        return new ItemStackTemplate(
            icon.instrumentItem(),
            1,
            DataComponentPatch.builder()
                .set(DataComponents.INSTRUMENT, new InstrumentComponent(getIcon(instruments, parentPeriod)))
                .build()
        );
    }

    private static Holder<Instrument> getIcon(Registry<Instrument> icons, int parentPeriod) {
        return icons.get(AbstractCycleIconRenderer.getIconIndex(icons.size(), parentPeriod))
            .or(icons::getAny)
            .orElse(null);
    }
}
