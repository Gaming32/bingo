package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.gaming32.bingo.ext.GlobalVars;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(BeehiveDecorator.class)
public abstract class MixinBeehiveDecorator {
    @WrapOperation(
        method = "place",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/feature/treedecorators/TreeDecorator$Context;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
        )
    )
    private void onSetBlock(
        TreeDecorator.Context instance, BlockPos pos, BlockState state,
        Operation<Void> original, @Share("block") LocalRef<BlockState> blockShare
    ) {
        original.call(instance, pos, state);
        blockShare.set(state);
    }

    @Inject(
        method = "place",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V",
            remap = false,
            shift = At.Shift.AFTER
        )
    )
    private void onPlace(
        TreeDecorator.Context context, CallbackInfo ci,
        @Local Optional<BlockPos> location, @Share("block") LocalRef<BlockState> blockShare
    ) {
        assert location.isPresent();
        if (GlobalVars.CURRENT_PLAYER.get() instanceof ServerPlayer player) {
            final ItemStack item = GlobalVars.CURRENT_ITEM.get();
            if (item != null) {
                BingoTriggers.GROW_BEE_NEST_TREE.get().trigger(player, location.get(), blockShare.get(), item);
            }
        }
    }
}
