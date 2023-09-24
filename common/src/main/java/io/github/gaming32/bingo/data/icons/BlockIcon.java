package io.github.gaming32.bingo.data.icons;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public record BlockIcon(BlockState block, ItemStack item) implements GoalIcon {
    public static final Codec<BlockIcon> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BlockState.CODEC.fieldOf("block").forGetter(BlockIcon::block),
            ItemStack.CODEC.optionalFieldOf("item").forGetter(i -> Optional.of(i.item))
        ).apply(instance, BlockIcon::ofFallbackItem)
    );

    @Environment(EnvType.CLIENT)
    private static final ItemTransform DEFAULT_TRANSFORM = new ItemTransform(
        new Vector3f(30f, 225f, 0f),
        new Vector3f(0f, 0f, 0f),
        new Vector3f(0.625f, 0.625f, 0.625f)
    );

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static BlockIcon ofFallbackItem(BlockState block, Optional<ItemStack> item) {
        return new BlockIcon(block, item.orElseGet(() -> stackFromBlock(block.getBlock())));
    }

    public static BlockIcon ofBlock(BlockState block) {
        return new BlockIcon(block, stackFromBlock(block.getBlock()));
    }

    public static BlockIcon ofBlock(Block block) {
        return new BlockIcon(block.defaultBlockState(), stackFromBlock(block));
    }

    private static ItemStack stackFromBlock(Block block) {
        return new ItemStack(block.asItem() != Items.AIR ? block : Blocks.STONE);
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
        renderInGui(block, graphics.pose(), graphics.bufferSource(), model);
        graphics.flush();
        if (flatLight) {
            Lighting.setupFor3DItems();
        }
        graphics.pose().popPose();
    }

    @Environment(EnvType.CLIENT)
    private static void renderInGui(BlockState state, PoseStack poseStack, MultiBufferSource buffer, BakedModel model) {
        poseStack.pushPose();
        ItemTransform transform = model.getTransforms().gui;
        if (transform == ItemTransform.NO_TRANSFORM) {
            transform = DEFAULT_TRANSFORM;
        }
        transform.apply(false, poseStack);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        renderModelLists(model, state, poseStack, buffer.getBuffer(ItemBlockRenderTypes.getRenderType(state, true)));
        poseStack.popPose();
    }

    @Environment(EnvType.CLIENT)
    private static void renderModelLists(BakedModel model, BlockState state, PoseStack poseStack, VertexConsumer buffer) {
        final RandomSource random = RandomSource.create();
        final long seed = 42;
        for (final Direction direction : Direction.values()) {
            random.setSeed(seed);
            renderQuadList(poseStack, buffer, model.getQuads(state, direction, random));
        }
        random.setSeed(seed);
        renderQuadList(poseStack, buffer, model.getQuads(state, null, random));
    }

    @Environment(EnvType.CLIENT)
    private static void renderQuadList(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads) {
        final PoseStack.Pose pose = poseStack.last();
        for (final BakedQuad quad : quads) {
            buffer.putBulkData(pose, quad, 1f, 1f, 1f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.BLOCK.get();
    }
}
