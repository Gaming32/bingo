package io.github.gaming32.bingo.client.icons;

import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import io.github.gaming32.bingo.data.icons.EntityIcon;
import io.github.gaming32.bingo.mixin.common.client.CameraAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.CacheSlot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.Map;
import java.util.WeakHashMap;

public class EntityIconRenderer implements IconRenderer<EntityIcon> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final CacheSlot<ClientLevel, Map<EntityIcon, Entity>> ENTITIES = new CacheSlot<>(l -> new WeakHashMap<>());

    @Override
    public void render(EntityIcon icon, GuiGraphics graphics, int x, int y) {
        final Entity entity = getEntity(icon);
        if (entity == null) return;
        renderEntity(entity, graphics, x, y);
    }

    public static void renderEntity(Entity entity, GuiGraphics graphics, int x, int y) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        float size = 14;
        if (Math.max(entity.getBbWidth(), entity.getBbHeight()) > 1) {
            size /= Math.max(entity.getBbWidth(), entity.getBbHeight());
        }
        if (entity instanceof LivingEntity living && living.isBaby()) {
            size /= 1.7f;
        }

        final Vector3f translation = new Vector3f(0, entity.getBbHeight() / 2 + 0.0625f, 0);
        final Quaternionf xRot = Axis.XP.rotationDegrees(-10f);
        final Quaternionf rotation = Axis.ZP.rotationDegrees(180f).mul(xRot);
        xRot.conjugate();
        entity.tickCount = minecraft.player.tickCount;
        entity.setYRot(-150f);
        entity.setYHeadRot(-150f);
        entity.setYBodyRot(-150f);
        entity.setXRot(0f);

        final EntityRenderDispatcher renderDispatcher = minecraft.getEntityRenderDispatcher();

        Vector2f topLeft = graphics.pose().transformPosition(x, y, new Vector2f());
        Vector2f bottomRight = graphics.pose().transformPosition(x + 16, y + 16, new Vector2f());

        EntityRenderer<? super Entity, ?> entityRenderer = renderDispatcher.getRenderer(entity);
        graphics.submitEntityRenderState(entityRenderer.createRenderState(entity, 1), size, translation, rotation, xRot, (int) topLeft.x, (int) topLeft.y, (int) bottomRight.x, (int) bottomRight.y);
    }

    @Nullable
    private static Entity getEntity(EntityIcon icon) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        return ENTITIES.compute(level).computeIfAbsent(icon, EntityIconRenderer::createEntity);
    }

    @Nullable
    private static Entity createEntity(EntityIcon icon) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        final Entity entity = icon.entity().create(level, EntitySpawnReason.LOAD);
        if (entity != null) {
            try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(new ProblemReporter.FieldPathElement("data"), LOGGER)) {
                ValueInput input = TagValueInput.create(collector, level.registryAccess(), icon.data());
                entity.load(input);
            }
        }
        return entity;
    }
}
