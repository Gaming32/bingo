package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.LivingEntityExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import io.github.gaming32.bingo.util.DamageEntry;
import net.minecraft.Optionull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
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
    private void onAddAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (!bingo$damageHistory.isEmpty()) {
            ListTag history = new ListTag();
            for (DamageEntry entry : bingo$damageHistory) {
                CompoundTag entryTag = entry.toNbt();
                if (entryTag != null) {
                    history.add(entryTag);
                }
            }
            compound.put("bingo:damage_history", history);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void onReadAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        bingo$damageHistory.clear();
        if (compound.contains("bingo:damage_history", Tag.TAG_LIST)) {
            ListTag types = compound.getList("bingo:damage_history", Tag.TAG_COMPOUND);
            for (int i = 0; i < types.size(); i++) {
                DamageEntry entry = DamageEntry.fromNbt(level().registryAccess(), types.getCompound(i));
                if (entry != null) {
                    bingo$damageHistory.add(entry);
                }
            }
        }
    }

    @Override
    public void bingo$recordDamage(DamageSource source) {
        bingo$damageHistory.add(new DamageEntry(
            Optionull.map(source.getEntity(), Entity::getType),
            Optionull.map(source.getDirectEntity(), Entity::getType),
            source.typeHolder()
        ));
    }

    @Override
    public boolean bingo$hasOnlyBeenDamagedBy(Predicate<DamageEntry> damageEntryPredicate) {
        return bingo$damageHistory.stream().allMatch(damageEntryPredicate);
    }
}
