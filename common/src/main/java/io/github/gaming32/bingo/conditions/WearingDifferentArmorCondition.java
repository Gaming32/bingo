package io.github.gaming32.bingo.conditions;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
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
    public static final Codec<WearingDifferentArmorCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "equipped_armor", MinMaxBounds.Ints.ANY).forGetter(WearingDifferentArmorCondition::equippedArmor),
            ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "different_types", MinMaxBounds.Ints.ANY).forGetter(WearingDifferentArmorCondition::differentTypes)
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
        int wearingCount = 0;
        final Set<ArmorMaterial> materials = Sets.newHashSetWithExpectedSize(4);
        for (final ItemStack stack : entity.getArmorSlots()) {
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
