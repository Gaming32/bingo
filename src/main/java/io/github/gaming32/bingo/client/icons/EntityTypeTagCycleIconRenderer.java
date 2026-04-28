package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.EntityTypeTagCycleIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class EntityTypeTagCycleIconRenderer implements AbstractCycleIconRenderer<EntityTypeTagCycleIcon> {
    private static final Map<ClientLevel, Map<EntityType<?>, Entity>> ENTITIES = new WeakHashMap<>();

    @Override
    public void renderWithParentPeriod(int parentPeriod, EntityTypeTagCycleIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        final var entityTypes = BuiltInRegistries.ENTITY_TYPE.get(icon.tag());
        if (entityTypes.isEmpty()) return;
        final var entityType = AbstractCycleIconRenderer.getIconFromTag(entityTypes.get(), parentPeriod);
        if (entityType.isEmpty()) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = ENTITIES.computeIfAbsent(level, k -> new HashMap<>())
            .computeIfAbsent(entityType.get().value(), k -> k.create(level, EntitySpawnReason.LOAD));
        if (entity == null) {
            return;
        }
        EntityIconRenderer.renderEntity(entity, graphics, x, y);
    }

    @Override
    public void renderDecorationsWithParentPeriod(int parentPeriod, EntityTypeTagCycleIcon icon, Font font, GuiGraphicsExtractor graphics, int x, int y) {
        IconRenderer.renderCount(icon.count(), font, graphics, x, y);
    }

    @Override
    public ItemStack getIconItemWithParentPeriod(int parentPeriod, EntityTypeTagCycleIcon icon) {
        final var entityTypes = BuiltInRegistries.ENTITY_TYPE.get(icon.tag());
        if (entityTypes.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final var entityType = AbstractCycleIconRenderer.getIconFromTag(entityTypes.get(), parentPeriod);
        if (entityType.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final Optional<Holder<Item>> spawnEggItem = SpawnEggItem.byId(entityType.get().value());
        return spawnEggItem.map(itemHolder -> new ItemStack(itemHolder, icon.count())).orElse(ItemStack.EMPTY);
    }
}
