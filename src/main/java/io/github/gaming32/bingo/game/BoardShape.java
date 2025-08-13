package io.github.gaming32.bingo.game;

import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.util.Vec2i;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public enum BoardShape implements StringRepresentable {
    SQUARE(0, "square", 1, 7) {
        @Override
        public int getGoalCount(int size) {
            return Mth.square(size);
        }

        @Override
        public List<int[]> getLines(int size) {
            List<int[]> lines = new ArrayList<>(2 * size + 2);

            for (int i = 0; i < size; i++) {
                int[] row = new int[size];
                for (int j = 0; j < size; j++) {
                    row[j] = i * size + j;
                }
                lines.add(row);

                int[] col = new int[size];
                for (int j = 0; j < size; j++) {
                    col[j] = j * size + i;
                }

                lines.add(col);
            }

            int[] diagonal1 = new int[size];
            for (int i = 0; i < size; i++) {
                diagonal1[i] = i * size + i;
            }
            lines.add(diagonal1);

            int[] diagonal2 = new int[size];
            for (int i = 0; i < size; i++) {
                diagonal2[i] = (size - i - 1) * size + i;
            }
            lines.add(diagonal2);

            return lines;
        }

        @Override
        public Vec2i getVisualSize(int size) {
            return new Vec2i(size, size);
        }

        @Override
        public Vec2i getCoords(int size, int cell) {
            return new Vec2i(cell % size, cell / size);
        }

        @Override
        public int getCellFromCoords(int size, int x, int y) {
            return y * size + x;
        }
    },
    NERF_EXPANDED(1, "nerf_expanded", 3, 6) {
        @Override
        public int getGoalCount(int size) {
            return Mth.square(size + 1) + 1;
        }

        @Override
        public List<int[]> getLines(int size) {
            List<int[]> lines = new ArrayList<>(2 * size + 2);

            for (int i = 0; i < size; i++) {
                int[] row = new int[size + 1];
                row[0] = Mth.square(size) + i;
                for (int j = 0; j < size; j++) {
                    row[j + 1] = i * size + j;
                }
                lines.add(row);

                int[] col = new int[size + 1];
                col[0] = Mth.square(size) + size + i;
                for (int j = 0; j < size; j++) {
                    col[j + 1] = j * size + i;
                }
                lines.add(col);
            }

            int[] diagonal1 = new int[size + 1];
            diagonal1[0] = Mth.square(size) + 2 * size;
            for (int i = 0; i < size; i++) {
                diagonal1[i + 1] = i * size + i;
            }
            lines.add(diagonal1);

            int[] diagonal2 = new int[size + 1];
            diagonal2[0] = Mth.square(size) + 2 * size + 1;
            for (int i = 0; i < size; i++) {
                diagonal2[i + 1] = (size - i - 1) * size + i;
            }
            lines.add(diagonal2);

            return lines;
        }

        @Override
        public Vec2i getVisualSize(int size) {
            return new Vec2i(size + 2, size + 2);
        }

        @Override
        public Vec2i getCoords(int size, int cell) {
            if (cell < Mth.square(size)) {
                // core grid
                return new Vec2i(cell % size + 1, cell / size + 1);
            } else if (cell < Mth.square(size) + size) {
                // row extensions
                int row = cell - Mth.square(size);
                return new Vec2i(row % 2 == 0 ? 0 : size + 1, row + 1);
            } else if (cell < Mth.square(size) + 2 * size) {
                // column extensions
                int col = cell - Mth.square(size) - size;
                return new Vec2i(col + 1, col % 2 == 0 ? 0 : size + 1);
            } else if (cell == Mth.square(size) + 2 * size) {
                // bottom left - top right diagonal extension
                return new Vec2i(0, size + 1);
            } else {
                // top left - bottom right diagonal extension
                return new Vec2i(size + 1, size + 1);
            }
        }

        @Override
        public int getCellFromCoords(int size, int x, int y) {
            if (x >= 1 && x <= size && y >= 1 && y <= size) {
                // core grid
                return (y - 1) * size + x - 1;
            } else if (y >= 1 && y <= size) {
                // row extension
                int expectedX = y % 2 == 0 ? size + 1 : 0;
                return x == expectedX ? Mth.square(size) + y - 1 : -1;
            } else if (x >= 1 && x <= size) {
                // column extension
                int expectedY = x % 2 == 0 ? size + 1 : 0;
                return y == expectedY ? Mth.square(size) + size + x - 1 : -1;
            } else if (x == 0 && y == size + 1) {
                // bottom left - top right diagonal extension
                return Mth.square(size) + 2 * size;
            } else if (x == size + 1 && y == size + 1) {
                // top left - bottom right diagonal extension
                return Mth.square(size) + 2 * size + 1;
            } else {
                return -1;
            }
        }

        @Override
        public boolean isNerfCell(int size, int cell) {
            return cell >= Mth.square(size);
        }
    };

    public static final Codec<BoardShape> CODEC = StringRepresentable.fromEnum(BoardShape::values);
    private static final IntFunction<BoardShape> BY_ID = ByIdMap.continuous(BoardShape::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, BoardShape> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, BoardShape::getId);

    private final int id;
    private final String name;
    private final int minSize;
    private final int maxSize;

    BoardShape(int id, String name, int minSize, int maxSize) {
        this.id = id;
        this.name = name;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public abstract int getGoalCount(int size);

    public abstract List<int[]> getLines(int size);

    public abstract Vec2i getVisualSize(int size);

    public abstract Vec2i getCoords(int size, int cell);

    public abstract int getCellFromCoords(int size, int x, int y);

    public boolean isNerfCell(int size, int cell) {
        return false;
    }

    public final int getMinSize() {
        return minSize;
    }

    public final int getMaxSize() {
        return maxSize;
    }

    @Override
    @NotNull
    public final String getSerializedName() {
        return name;
    }

    private int getId() {
        return id;
    }
}
