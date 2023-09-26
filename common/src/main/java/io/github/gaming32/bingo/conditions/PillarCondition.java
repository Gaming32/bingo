package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

public record PillarCondition(int minHeight, Optional<BlockPredicate> block) implements LootItemCondition {
    public static final Codec<PillarCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("min_height").forGetter(PillarCondition::minHeight),
            ExtraCodecs.strictOptionalField(BlockPredicate.CODEC, "block").forGetter(PillarCondition::block)
        ).apply(instance, PillarCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.PILLAR.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        final ServerLevel level = lootContext.getLevel();
        final BlockPos.MutableBlockPos pos = BlockPos.containing(lootContext.getParam(LootContextParams.ORIGIN)).mutable();
        final BiPredicate<ServerLevel, BlockPos> predicate = block.isPresent()
            ? block.get()::matches
            : (l, b) -> !l.getBlockState(b).isAir();

        int height = 0;
        while (true) {
            if (!predicate.test(level, pos)) {
                return false;
            }
            height++;
            if (height >= minHeight) {
                return true;
            }
            pos.move(0, 1, 0);
        }
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }
}
