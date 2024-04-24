package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public enum StairwayToHeavenCondition implements LootItemCondition {
    INSTANCE;

    public static final MapCodec<StairwayToHeavenCondition> CODEC = MapCodec.unit(INSTANCE);

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.STAIRWAY_TO_HEAVEN.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        final Entity thisEntity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        final BlockPos origin = thisEntity.getOnPos();
        if (!(thisEntity.level() instanceof ServerLevel level)) {
            return false;
        }

        final BlockState originState = level.getBlockState(origin);
        if (
            originState.is(BlockTags.STAIRS) &&
                scan(level, origin, originState.getValue(StairBlock.FACING).getOpposite())
        ) {
            return true;
        }

        return scan(level, origin.north(), Direction.NORTH)
            || scan(level, origin.south(), Direction.SOUTH)
            || scan(level, origin.east(), Direction.EAST)
            || scan(level, origin.west(), Direction.WEST);
    }

    private static boolean scan(ServerLevel level, BlockPos startPos, Direction direction) {
        final ServerChunkCache chunkSource = level.getChunkSource();
        final ChunkGenerator generator = chunkSource.getGenerator();
        final RandomState randomState = chunkSource.randomState();

        final Direction stairDirection = direction.getOpposite();
        final BlockPos.MutableBlockPos currentPos = startPos.mutable();
        while (true) {
            final BlockState state = level.getBlockState(currentPos);
            if (
                !state.is(BlockTags.STAIRS) ||
                    state.getValue(StairBlock.FACING) != stairDirection ||
                    state.getValue(StairBlock.HALF) != Half.BOTTOM
            ) {
                return generator.getFirstOccupiedHeight(
                    currentPos.getX(), currentPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, level, randomState
                ) >= currentPos.getY();
            }
            currentPos.move(direction.getStepX(), -1, direction.getStepZ());
        }
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }
}
