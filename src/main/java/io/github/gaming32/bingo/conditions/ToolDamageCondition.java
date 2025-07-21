package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record ToolDamageCondition(MinMaxBounds.Ints damage) implements LootItemCondition {
    public static final MapCodec<ToolDamageCondition> CODEC = MinMaxBounds.Ints.CODEC
        .fieldOf("damage")
        .xmap(ToolDamageCondition::new, ToolDamageCondition::damage);

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.TOOL_DAMAGE.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack tool = lootContext.getParameter(LootContextParams.TOOL);
        return tool.isDamageableItem() && damage.matches(tool.getDamageValue());
    }

    @NotNull
    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }
}
