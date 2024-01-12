package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.EntityTypeTagCycleIcon;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class EntityTypeTagCycleIconRenderer implements AbstractCycleIconRenderer<EntityTypeTagCycleIcon> {
    private static final Map<ClientLevel, Map<EntityType<?>, Entity>> ENTITIES = new WeakHashMap<>();

    @Override
    public void renderWithParentPeriod(int parentPeriod, EntityTypeTagCycleIcon icon, GuiGraphics graphics, int x, int y) {
        final var entityTypes = BuiltInRegistries.ENTITY_TYPE.getTag(icon.tag());
        if (entityTypes.isEmpty() || entityTypes.get().size() == 0) return;
        EntityType<?> entityType = getIcon(entityTypes.get(), parentPeriod).value();

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = ENTITIES.computeIfAbsent(level, k -> new HashMap<>()).computeIfAbsent(entityType, k -> entityType.create(level));
        if (entity == null) {
            return;
        }
        EntityIconRenderer.renderEntity(entity, graphics, x, y);
    }

    @Override
    public void renderDecorationsWithParentPeriod(int parentPeriod, EntityTypeTagCycleIcon icon, Font font, GuiGraphics graphics, int x, int y) {
        IconRenderer.renderCount(icon.count(), font, graphics, x, y);
    }

    private static Holder<EntityType<?>> getIcon(HolderSet.Named<EntityType<?>> icons, int parentPeriod) {
        return icons.get((int)((Util.getMillis() / (CycleIconRenderer.TIME_PER_ICON * parentPeriod)) % icons.size()));
    }
}
