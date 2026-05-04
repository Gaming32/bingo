package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

public record OneByOneHoleCondition(int bottom, int top, BlockPredicate predicate) implements LootItemCondition {
    public static final MapCodec<OneByOneHoleCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.INT.fieldOf("bottom").forGetter(OneByOneHoleCondition::bottom),
            Codec.INT.fieldOf("top").forGetter(OneByOneHoleCondition::top),
            BlockPredicate.CODEC.fieldOf("predicate").forGetter(OneByOneHoleCondition::predicate)
        ).apply(instance, OneByOneHoleCondition::new)
    );

    @Override
    public MapCodec<OneByOneHoleCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(LootContext lootContext) {
        final ServerLevel level = lootContext.getLevel();
        final BlockPos.MutableBlockPos pos = BlockPos.containing(lootContext.getParameter(LootContextParams.ORIGIN)).mutable();
        for (int y = bottom; y <= top; y++) {
            if (!predicate.test(level, pos.setY(y))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }
}
