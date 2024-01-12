package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public record ToolIsEnchantedCondition(boolean nonCurse) implements LootItemCondition {
    public static final Codec<ToolIsEnchantedCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ExtraCodecs.strictOptionalField(Codec.BOOL, "non_curse", false).forGetter(ToolIsEnchantedCondition::nonCurse)
        ).apply(instance, ToolIsEnchantedCondition::new)
    );

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
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(tool);
        if (nonCurse) {
            return !enchantments.keySet().stream().allMatch(Enchantment::isCurse);
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
