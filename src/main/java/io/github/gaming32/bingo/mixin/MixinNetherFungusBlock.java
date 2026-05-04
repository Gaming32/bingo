package io.github.gaming32.bingo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.triggers.GrowFeatureTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.NetherFungusBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetherFungusBlock.class)
public class MixinNetherFungusBlock {
    @WrapOperation(
        method = "lambda$performBonemeal$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/feature/ConfiguredFeature;place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z",
            remap = true
        ),
        remap = false
    )
    private static boolean onPlaceFungus(
        ConfiguredFeature<?, ?> feature,
        WorldGenLevel level,
        ChunkGenerator chunkGenerator,
        RandomSource random,
        BlockPos origin,
        Operation<Boolean> operation
    ) {
        return GrowFeatureTrigger.wrapPlaceOperation(feature, level, chunkGenerator, random, origin, operation);
    }
}
