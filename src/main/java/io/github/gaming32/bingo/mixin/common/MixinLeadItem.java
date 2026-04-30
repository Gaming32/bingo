package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.GlobalVars;
import io.github.gaming32.bingo.ext.LeashFenceKnotEntityExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeadItem.class)
public class MixinLeadItem {
    @Inject(
        method = "bindPlayerMobs",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;iterator()Ljava/util/Iterator;"
        )
    )
    private static void setKnotOwner(
        Player player, Level level, BlockPos pos, CallbackInfoReturnable<InteractionResult> cir, @Local(name = "activeKnot") LeashFenceKnotEntity activeKnot
    ) {
        ((LeashFenceKnotEntityExt) activeKnot).bingo$setOwner(player);  // todo: not sure if this works
    }

    @WrapOperation(
        method = "useOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/LeadItem;bindPlayerMobs(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/InteractionResult;"
        )
    )
    private InteractionResult storeCurrentItem(
        Player player, Level level, BlockPos pos,
        Operation<InteractionResult> original,
        @Local(argsOnly = true) UseOnContext context
    ) {
        try (final var _ = GlobalVars.CURRENT_ITEM.push(context.getItemInHand())) {
            return original.call(player, level, pos);
        }
    }

    @WrapOperation(
        method = "bindPlayerMobs",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Leashable;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V"
        )
    )
    private static void triggerLeashedEntity(
        Leashable instance, Entity holder, boolean synch, Operation<Void> original,
        @Local(argsOnly = true) Player player,
        @Local(argsOnly = true) BlockPos pos
    ) {
        original.call(instance, holder, synch);
        if (player instanceof ServerPlayer serverPlayer) {
            BingoTriggers.LEASHED_ENTITY.get().trigger(
                serverPlayer, (Entity)instance, holder, pos,
                GlobalVars.CURRENT_ITEM.getOrElse(ItemStack.EMPTY)
            );
        }
    }
}
