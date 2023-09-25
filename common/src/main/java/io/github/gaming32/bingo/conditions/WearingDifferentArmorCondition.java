package io.github.gaming32.bingo.conditions;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.MinMaxBounds;
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

public class WearingDifferentArmorCondition implements LootItemCondition {
    private final MinMaxBounds.Ints equippedArmor;
    private final MinMaxBounds.Ints differentTypes;

    public WearingDifferentArmorCondition(MinMaxBounds.Ints equippedArmor, MinMaxBounds.Ints differentTypes) {
        this.equippedArmor = equippedArmor;
        this.differentTypes = differentTypes;
    }

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

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<WearingDifferentArmorCondition> {
        @Override
        public void serialize(JsonObject json, WearingDifferentArmorCondition value, JsonSerializationContext serializationContext) {
            json.add("equipped_armor", value.equippedArmor.serializeToJson());
            json.add("different_types", value.differentTypes.serializeToJson());
        }

        @NotNull
        @Override
        public WearingDifferentArmorCondition deserialize(JsonObject json, JsonDeserializationContext serializationContext) {
            return new WearingDifferentArmorCondition(
                MinMaxBounds.Ints.fromJson(json.get("equipped_armor")),
                MinMaxBounds.Ints.fromJson(json.get("different_types"))
            );
        }
    }
}
