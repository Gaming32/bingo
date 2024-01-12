package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record ToolDamageCondition(MinMaxBounds.Ints damage) implements LootItemCondition {
    public static final Codec<ToolDamageCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            MinMaxBounds.Ints.CODEC.fieldOf("damage").forGetter(ToolDamageCondition::damage)
        ).apply(instance, ToolDamageCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.TOOL_DAMAGE.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack tool = lootContext.getParam(LootContextParams.TOOL);
        return tool.isDamageableItem() && damage.matches(tool.getDamageValue());
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }
}
