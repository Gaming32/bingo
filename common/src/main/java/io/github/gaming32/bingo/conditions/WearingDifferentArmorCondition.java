package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public record WearingDifferentArmorCondition(
    MinMaxBounds.Ints equippedArmor,
    MinMaxBounds.Ints differentTypes,
    List<HolderSet<Item>> armorTypes
) implements LootItemCondition {
    public static final MapCodec<WearingDifferentArmorCondition> CODEC = RecordCodecBuilder.<WearingDifferentArmorCondition>mapCodec(instance ->
        instance.group(
            MinMaxBounds.Ints.CODEC
                .optionalFieldOf("equipped_armor", MinMaxBounds.Ints.ANY)
                .forGetter(WearingDifferentArmorCondition::equippedArmor),
            MinMaxBounds.Ints.CODEC
                .optionalFieldOf("different_types", MinMaxBounds.Ints.ANY)
                .forGetter(WearingDifferentArmorCondition::differentTypes),
            Ingredient.NON_AIR_HOLDER_SET_CODEC
                .listOf()
                .optionalFieldOf("armor_types", List.of())
                .forGetter(WearingDifferentArmorCondition::armorTypes)
        ).apply(instance, WearingDifferentArmorCondition::new)
    ).validate(condition -> {
        if (
            condition.differentTypes.min().isPresent() &&
            condition.differentTypes.min().get() > condition.armorTypes.size()
        ) {
            return DataResult.error(
                () -> "wearing_different_armor condition is impossible, as more armor types are required than are specified.",
                condition
            );
        }
        return DataResult.success(condition);
    });

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
        final var unmatchedTypes = new IntOpenHashSet(BingoUtil.generateIntArray(armorTypes.size()));
        for (final ItemStack stack : livingEntity.getArmorAndBodyArmorSlots()) {
            final var unmatchedIter = unmatchedTypes.iterator();
            while (unmatchedIter.hasNext()) {
                final var armorType = armorTypes.get(unmatchedIter.nextInt());
                if (armorType.contains(stack.getItemHolder())) {
                    unmatchedIter.remove();
                }
            }
        }
        return equippedArmor.matches(wearingCount) && differentTypes.matches(armorTypes.size() - unmatchedTypes.size());
    }

    @NotNull
    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }
}
