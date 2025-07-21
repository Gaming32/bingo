package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.mixin.common.BaseFireBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public enum FlammableCondition implements LootItemCondition {
    INSTANCE;

    public static final MapCodec<FlammableCondition> CODEC = MapCodec.unit(INSTANCE);

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.FLAMMABLE.get();
    }

    @NotNull
    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext lootContext) {
        final BlockPos pos = BlockPos.containing(lootContext.getParameter(LootContextParams.ORIGIN));
        return ((BaseFireBlockAccessor)Blocks.FIRE).callCanBurn(lootContext.getLevel().getBlockState(pos));
    }
}
