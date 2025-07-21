package io.github.gaming32.bingo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * A rewrite of Mojang's BlockPattern because it is broken
 */
public class BlockPattern {
    private final Predicate<BlockInWorld>[][][] predicates;
    private final int xSize, ySize, zSize;

    public BlockPattern(Predicate<BlockInWorld>[][][] predicates) {
        this.predicates = predicates;
        this.zSize = predicates.length;
        this.ySize = Arrays.stream(predicates).mapToInt(aisle -> aisle.length).max().orElse(0);
        this.xSize = Arrays.stream(predicates).flatMapToInt(aisle -> Arrays.stream(aisle).mapToInt(row -> row.length)).max().orElse(0);
    }

    private static void transformPos(BlockPos origin, BlockPos pos, Direction right, Direction up, Direction forward, BlockPos.MutableBlockPos outPos) {
        outPos.set(
            pos.getX() - origin.getX(),
            pos.getY() - origin.getY(),
            pos.getZ() - origin.getZ()
        );
        outPos.set(
            outPos.getX() * right.getStepX() + outPos.getY() * up.getStepX() + outPos.getZ() * forward.getStepX() + origin.getX(),
            outPos.getX() * right.getStepY() + outPos.getY() * up.getStepY() + outPos.getZ() * forward.getStepY() + origin.getY(),
            outPos.getX() * right.getStepZ() + outPos.getY() * up.getStepZ() + outPos.getZ() * forward.getStepZ() + origin.getZ()
        );
    }

    private boolean check(LevelReader level, BlockPos origin, BlockPos checkAt, Direction right, Direction up, Direction forward) {
        BlockPos.MutableBlockPos tempPos = new BlockPos.MutableBlockPos();
        for (BlockPos pos : BlockPos.betweenClosed(checkAt, checkAt.offset(xSize - 1, ySize - 1, zSize - 1))) {
            transformPos(origin, pos, right, up, forward, tempPos);
            if (!predicates[pos.getZ() - checkAt.getZ()][pos.getY() - checkAt.getY()][pos.getX() - checkAt.getX()].test(new BlockInWorld(level, tempPos, false))) {
                return false;
            }
        }
        return true;
    }

    private boolean find(LevelReader level, BlockPos origin, Direction right, Direction up, Direction forward) {
        for (BlockPos checkAt : BlockPos.betweenClosed(origin.subtract(new Vec3i(xSize - 1, ySize - 1, zSize - 1)), origin)) {
            if (check(level, origin, checkAt, right, up, forward)) {
                return true;
            }
        }
        return false;
    }

    public boolean find(LevelReader level, BlockPos origin, Rotations rotations) {
        if (xSize == 0 || ySize == 0 || zSize == 0) {
            // an empty structure always exists
            return true;
        }

        return switch (rotations) {
            case NONE -> find(level, origin, Direction.EAST, Direction.UP, Direction.NORTH);
            case HORIZONTAL -> {
                for (Direction right : Direction.Plane.HORIZONTAL) {
                    for (Direction forward : Direction.Plane.HORIZONTAL) {
                        if (right.getAxis() != forward.getAxis() && find(level, origin, right, Direction.UP, forward)) {
                            yield true;
                        }
                    }
                }
                yield false;
            }
            case ALL -> {
                for (Direction right : Direction.values()) {
                    for (Direction forward : Direction.values()) {
                        if (forward.getAxis() == right.getAxis()) {
                            continue;
                        }
                        for (Direction up : Direction.values()) {
                            if (up.getAxis() == right.getAxis() || up.getAxis() == forward.getAxis()) {
                                continue;
                            }
                            if (find(level, origin, right, up, forward)) {
                                yield true;
                            }
                        }
                    }
                }
                yield false;
            }
        };
    }

    public enum Rotations implements StringRepresentable {
        NONE("none"), HORIZONTAL("horizontal"), ALL("all");

        @SuppressWarnings("deprecation")
        public static final EnumCodec<Rotations> CODEC = StringRepresentable.fromEnum(Rotations::values);

        private final String serializedName;

        Rotations(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        @NotNull
        public String getSerializedName() {
            return serializedName;
        }
    }
}
