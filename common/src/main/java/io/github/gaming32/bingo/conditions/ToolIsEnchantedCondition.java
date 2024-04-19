package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record ToolIsEnchantedCondition(boolean nonCurse) implements LootItemCondition {
    public static final MapCodec<ToolIsEnchantedCondition> CODEC = Codec.BOOL
        .optionalFieldOf("non_curse", false)
        .xmap(ToolIsEnchantedCondition::new, ToolIsEnchantedCondition::nonCurse);

    public ToolIsEnchantedCondition() {
        this(false);
    }

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.TOOL_IS_ENCHANTED.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack tool = lootContext.getParam(LootContextParams.TOOL);
        ItemEnchantments enchantments = tool.getEnchantments();
        if (nonCurse) {
            return !enchantments.keySet().stream().map(Holder::value).allMatch(Enchantment::isCurse);
        } else {
            return !enchantments.isEmpty();
        }
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }
}
