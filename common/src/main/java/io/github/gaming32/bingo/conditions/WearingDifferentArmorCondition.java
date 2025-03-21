package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.data.tags.convention.ConventionItemTags;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public record WearingDifferentArmorCondition(
    MinMaxBounds.Ints equippedArmor,
    MinMaxBounds.Ints differentTypes
) implements LootItemCondition {
    public static final MapCodec<WearingDifferentArmorCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            MinMaxBounds.Ints.CODEC
                .optionalFieldOf("equipped_armor", MinMaxBounds.Ints.ANY)
                .forGetter(WearingDifferentArmorCondition::equippedArmor),
            MinMaxBounds.Ints.CODEC
                .optionalFieldOf("different_types", MinMaxBounds.Ints.ANY)
                .forGetter(WearingDifferentArmorCondition::differentTypes)
        ).apply(instance, WearingDifferentArmorCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.WEARING_DIFFERENT_ARMOR.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        final Entity entity = lootContext.getParameter(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        int wearingCount = 0;
        final var models = HashSet.<ResourceKey<EquipmentAsset>>newHashSet(4);
        for (final var slot : EquipmentSlotGroup.ARMOR) {
            final var stack = livingEntity.getItemBySlot(slot);
            if (!stack.is(ConventionItemTags.ARMORS)) continue;
            final var equippable = stack.get(DataComponents.EQUIPPABLE);
            if (equippable == null) continue;
            equippable.assetId().ifPresent(models::add);
        }
        return equippedArmor.matches(wearingCount) && differentTypes.matches(models.size());
    }

    @NotNull
    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }
}
