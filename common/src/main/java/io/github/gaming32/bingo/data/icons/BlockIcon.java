package io.github.gaming32.bingo.data.icons;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.Optional;

public record BlockIcon(BlockState block, ItemStack item) implements GoalIcon {
    public static final Codec<BlockIcon> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BlockState.CODEC.fieldOf("block").forGetter(BlockIcon::block),
            ItemStack.CODEC.optionalFieldOf("item").forGetter(i -> Optional.of(i.item))
        ).apply(instance, BlockIcon::ofFallbackItem)
    );

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static BlockIcon ofFallbackItem(BlockState block, Optional<ItemStack> item) {
        return new BlockIcon(block, item.orElseGet(() -> new ItemStack(block.getBlock())));
    }

    public static BlockIcon ofBlock(BlockState block) {
        return new BlockIcon(block, new ItemStack(block.getBlock()));
    }

    public static BlockIcon ofBlock(Block block) {
        return new BlockIcon(block.defaultBlockState(), new ItemStack(block));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        final Minecraft minecraft = Minecraft.getInstance();
        final BakedModel model = minecraft.getBlockRenderer().getBlockModel(block);
        graphics.pose().pushPose();
        graphics.pose().translate(x + 8f, y + 8f, 150f);
        graphics.pose().mulPoseMatrix(new Matrix4f().scaling(1f, -1f, 1f));
        graphics.pose().scale(16f, 16f, 16f);
        final boolean flatLight = !model.usesBlockLight();
        if (flatLight) {
            Lighting.setupForFlatItems();
        }
        minecraft.getItemRenderer().render(
            item, ItemDisplayContext.GUI, false, graphics.pose(),
            graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model
        );
        graphics.flush();
        if (flatLight) {
            Lighting.setupFor3DItems();
        }
        graphics.pose().popPose();
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.BLOCK.get();
    }
}
