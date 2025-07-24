package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record BlockCondition(BlockPredicate block) implements LootItemCondition {
    public static final MapCodec<BlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BlockPredicate.CODEC.fieldOf("block").forGetter(BlockCondition::block)
    ).apply(instance, BlockCondition::new));

    @Override
    @NotNull
    public LootItemConditionType getType() {
        return BingoConditions.BLOCK.get();
    }

    @Override
    @NotNull
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext lootContext) {
        BlockPos pos = BlockPos.containing(lootContext.getParameter(LootContextParams.ORIGIN));
        return block.matches(lootContext.getLevel(), pos);
    }

    public static Builder builder(BlockPredicate block) {
        return new Builder(block);
    }

    public static Builder builder(BlockPredicate.Builder blockBuilder) {
        return builder(blockBuilder.build());
    }

    public static class Builder implements LootItemCondition.Builder {
        private final BlockPredicate block;

        private Builder(BlockPredicate block) {
            this.block = block;
        }

        @Override
        @NotNull
        public LootItemCondition build() {
            return new BlockCondition(block);
        }
    }
}
