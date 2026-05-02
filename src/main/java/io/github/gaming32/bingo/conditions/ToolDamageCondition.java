package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

public record ToolDamageCondition(MinMaxBounds.Ints damage) implements LootItemCondition {
    public static final MapCodec<ToolDamageCondition> CODEC = MinMaxBounds.Ints.CODEC
        .fieldOf("damage")
        .xmap(ToolDamageCondition::new, ToolDamageCondition::damage);

    @Override
    public MapCodec<ToolDamageCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack tool = BingoUtil.toItemStack(lootContext.getParameter(LootContextParams.TOOL));
        return tool.isDamageableItem() && damage.matches(tool.getDamageValue());
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }
}
