package io.github.gaming32.bingo.conditions;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record WearingDifferentArmorCondition(
    MinMaxBounds.Ints equippedArmor,
    MinMaxBounds.Ints differentTypes
) implements LootItemCondition {
    public static final MapCodec<WearingDifferentArmorCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            MinMaxBounds.Ints.CODEC.optionalFieldOf("equipped_armor", MinMaxBounds.Ints.ANY).forGetter(WearingDifferentArmorCondition::equippedArmor),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("different_types", MinMaxBounds.Ints.ANY).forGetter(WearingDifferentArmorCondition::differentTypes)
        ).apply(instance, WearingDifferentArmorCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.WEARING_DIFFERENT_ARMOR.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        final Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        int wearingCount = 0;
        final Set<Holder<ArmorMaterial>> materials = Sets.newHashSetWithExpectedSize(4);
        for (final ItemStack stack : livingEntity.getArmorSlots()) {
            if (!(stack.getItem() instanceof ArmorItem armorItem)) continue;
            wearingCount++;
            materials.add(armorItem.getMaterial());
        }
        return equippedArmor.matches(wearingCount) && differentTypes.matches(materials.size());
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }
}
