package io.github.gaming32.bingo.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.LeashFenceKnotEntityExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(LeashFenceKnotEntity.class)
public abstract class MixinLeashFenceKnotEntity extends BlockAttachedEntity implements LeashFenceKnotEntityExt {
    @Unique
    private Player bingo$cachedOwner;
    @Unique
    private UUID bingo$ownerUuid;

    protected MixinLeashFenceKnotEntity(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @Nullable Player bingo$getOwner() {
        if (bingo$cachedOwner == null) {
            final UUID uuid = bingo$ownerUuid;
            if (uuid == null) {
                return null;
            }
            final Player player = level().getPlayerByUUID(uuid);
            if (player == null) {
                return null;
            }
            bingo$cachedOwner = player;
            bingo$ownerUuid = null;
            return player;
        }
        return bingo$cachedOwner;
    }

    @Override
    public void bingo$setOwner(@Nullable Player player) {
        bingo$cachedOwner = player;
        bingo$ownerUuid = null;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void writeOwner(ValueOutput output, CallbackInfo ci) {
        final UUID ownerUuid;
        if (bingo$cachedOwner != null) {
            ownerUuid = bingo$cachedOwner.getUUID();
        } else if (bingo$ownerUuid != null) {
            ownerUuid = bingo$ownerUuid;
        } else {
            return;
        }
        output.store("bingo:owner", UUIDUtil.CODEC, ownerUuid);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readOwner(ValueInput input, CallbackInfo ci) {
        final UUID ownerUuid = input.read("bingo:owner", UUIDUtil.CODEC).orElse(null);
        if (bingo$cachedOwner != null && bingo$cachedOwner.getUUID().equals(ownerUuid)) return;
        bingo$ownerUuid = ownerUuid;
        bingo$cachedOwner = null;
    }

    @Definition(id = "leashable", local = @Local(type = Leashable.class, name = "leashable"))
    @Definition(id = "setLeashedTo", method = "Lnet/minecraft/world/entity/Leashable;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V")
    @Expression("leashable.setLeashedTo(this, true)")
    @Inject(method = "interact", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void onLeashEntityToThis(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir, @Local(name = "leashable") Leashable leashable) {
        if (player instanceof ServerPlayer serverPlayer) {
            BingoTriggers.LEASHED_ENTITY.get().trigger(
                serverPlayer,
                (Entity) leashable,
                this,
                BlockPos.containing(location),
                player.getItemInHand(hand)
            );
        }
    }

    @Inject(method = "interact", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/gameevent/GameEvent;BLOCK_ATTACH:Lnet/minecraft/core/Holder$Reference;", opcode = Opcodes.GETSTATIC))
    private void onAttach(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir, @Local(name = "attachedMob") boolean attachedMob) {
        if (attachedMob) {
            bingo$setOwner(player);
        }
    }
}
