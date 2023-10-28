package io.github.gaming32.bingo.client.icons;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.gaming32.bingo.data.icons.EntityIcon;
import io.github.gaming32.bingo.mixin.common.client.CameraAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

import java.util.Map;
import java.util.WeakHashMap;

public class EntityIconRenderer implements IconRenderer<EntityIcon> {
    private static final Map<ClientLevel, Map<EntityIcon, Entity>> ENTITIES = new WeakHashMap<>();

    @Override
    public void render(EntityIcon icon, GuiGraphics graphics, int x, int y) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        final Entity entity = getEntity(icon);
        if (entity == null) return;

        Lighting.setupForFlatItems();

        final PoseStack pose = graphics.pose();
        pose.pushPose();

        float size = 14;
        if (Math.max(entity.getBbWidth(), entity.getBbHeight()) > 1) {
            size /= Math.max(entity.getBbWidth(), entity.getBbHeight());
        }
        int yOffset = 15;
        if (entity instanceof LivingEntity living && living.isBaby()) {
            size /= 1.7f;
        }

        pose.translate(x + 8f, y + yOffset, 1050f);
        pose.scale(1f, 1f, -1f);
        pose.translate(0f, 0f, 1000f);
        pose.scale(size, size, size);

        final Quaternionf xRot = Axis.XP.rotationDegrees(-10f);
        pose.mulPose(Axis.ZP.rotationDegrees(180f).mul(xRot));
        if (minecraft.cameraEntity != null) {
            entity.setPos(
                minecraft.cameraEntity.getX(),
                minecraft.cameraEntity.getY(),
                minecraft.cameraEntity.getZ()
            );
        }
        entity.tickCount = minecraft.player.tickCount;
        entity.setYRot(-150f);
        entity.setYHeadRot(-150f);
        entity.setYBodyRot(-150f);
        entity.setXRot(0f);

        final EntityRenderDispatcher renderDispatcher = minecraft.getEntityRenderDispatcher();
        xRot.conjugate();
        ((CameraAccessor)renderDispatcher.camera).setYRot(0f);
        renderDispatcher.overrideCameraOrientation(xRot);
        renderDispatcher.setRenderShadow(false);

        final MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        renderDispatcher.render(entity, 0, 0, 0, -150f, 1f, pose, bufferSource, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();

        renderDispatcher.setRenderShadow(true);
        pose.popPose();

        Lighting.setupFor3DItems();
    }

    public static Entity getEntity(EntityIcon icon) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        return ENTITIES.computeIfAbsent(level, l -> new WeakHashMap<>())
            .computeIfAbsent(icon, EntityIconRenderer::createEntity);
    }

    private static Entity createEntity(EntityIcon icon) {
        final ClientLevel level = Minecraft.getInstance().level;
        final Entity entity = icon.entity().create(level);
        if (entity != null) {
            entity.load(icon.data());
        }
        return entity;
    }
}
