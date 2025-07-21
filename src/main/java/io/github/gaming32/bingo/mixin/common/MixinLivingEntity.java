package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.ItemEntityExt;
import io.github.gaming32.bingo.ext.LivingEntityExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import io.github.gaming32.bingo.util.DamageEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements LivingEntityExt {
    @Unique
    private final Set<DamageEntry> bingo$damageHistory = new HashSet<>();

    public MixinLivingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method = "onEquipItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;doesEmitEquipEvent(Lnet/minecraft/world/entity/EquipmentSlot;)Z"
        )
    )
    @SuppressWarnings("UnreachableCode")
    private void onEquipItem(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayer player) {
            BingoTriggers.EQUIP_ITEM.get().trigger(player, oldItem, newItem, slot);
        }
    }

    @Inject(
        method = "hurtServer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"
        )
    )
    private void onDeathFromDamageSource(
        ServerLevel serverLevel, DamageSource damageSource, float taken,
        CallbackInfoReturnable<Boolean> cir,
        @Local(ordinal = 1) float dealt,
        @Local(ordinal = 0) boolean blocked
    ) {
        BingoTriggers.ENTITY_DIE_NEAR_PLAYER.get().trigger((LivingEntity) (Object) this, damageSource, dealt, taken, blocked);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void onAddAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        if (!bingo$damageHistory.isEmpty()) {
            final var outputList = output.list("bingo:damage_history", DamageEntry.CODEC);
            bingo$damageHistory.forEach(outputList::add);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void onReadAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        bingo$damageHistory.clear();
        input.listOrEmpty("bingo:damage_history", DamageEntry.CODEC).forEach(bingo$damageHistory::add);
    }

    @Inject(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("RETURN")
    )
    private void setDroppedBy(ItemStack droppedItem, boolean dropAround, boolean includeThrowerName, CallbackInfoReturnable<ItemEntity> cir) {
        if (cir.getReturnValue() instanceof ItemEntityExt itemEntity) {
            itemEntity.bingo$setDroppedBy(this);
        }
    }

    @Override
    public void bingo$recordDamage(DamageSource source) {
        bingo$damageHistory.add(new DamageEntry(
            Optional.ofNullable(source.getEntity()).map(Entity::getType),
            Optional.ofNullable(source.getDirectEntity()).map(Entity::getType),
            source.typeHolder()
        ));
    }

    @Override
    public boolean bingo$hasOnlyBeenDamagedBy(Predicate<DamageEntry> damageEntryPredicate) {
        return bingo$damageHistory.stream().allMatch(damageEntryPredicate);
    }
}
