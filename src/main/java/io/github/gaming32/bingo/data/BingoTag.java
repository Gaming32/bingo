package io.github.gaming32.bingo.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoStreamCodecs;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatImmutableList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public record BingoTag(FloatList difficultyMax, boolean allowedOnSameLine, SpecialType specialType) {
    private static final Codec<FloatList> DIFFICULTY_MAX_CODEC = ExtraCodecs.nonEmptyList(
        Codec.FLOAT.validate(
            f -> f >= 0f && f <= 1f
                ? DataResult.success(f)
                : DataResult.error(() -> "Value in difficulty_max must be in range [0,1]")
        ).listOf()
    ).xmap(FloatImmutableList::new, Function.identity());
    public static final Codec<BingoTag> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            DIFFICULTY_MAX_CODEC.fieldOf("difficulty_max").forGetter(BingoTag::difficultyMax),
            Codec.BOOL.optionalFieldOf("allowed_on_same_line", true).forGetter(BingoTag::allowedOnSameLine),
            SpecialType.CODEC.optionalFieldOf("special_type", SpecialType.NONE).forGetter(BingoTag::specialType)
        ).apply(instance, BingoTag::new)
    );

    public float getUnscaledMaxForDifficulty(int difficulty) {
        if (difficulty < 0) {
            throw new IllegalArgumentException("difficulty < 0 is invalid");
        }
        final int size = difficultyMax.size();
        return difficulty < size ? difficultyMax.getFloat(difficulty) : difficultyMax.getFloat(size - 1);
    }

    public int getMaxForDifficulty(int difficulty, int goalCount) {
        return Mth.ceil(getUnscaledMaxForDifficulty(difficulty) * goalCount);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private FloatList difficultyMax = new FloatArrayList();
        private boolean allowedOnSameLine = true;
        private SpecialType specialType = SpecialType.NONE;

        private Builder() {
        }

        public Builder difficultyMax(int... scaledBy5x5) {
            final float[] unscaled = new float[scaledBy5x5.length];
            for (int i = 0; i < scaledBy5x5.length; i++) {
                unscaled[i] = scaledBy5x5[i] / 25f;
            }
            return this.difficultyMax(unscaled);
        }

        public Builder difficultyMax(float... unscaledMaxes) {
            this.difficultyMax = FloatList.of(unscaledMaxes);
            return this;
        }

        public Builder markerTag() {
            this.difficultyMax = FloatList.of(1f);
            return this;
        }

        public Builder disallowOnSameLine() {
            this.allowedOnSameLine = false;
            return this;
        }

        public Builder specialType(SpecialType specialType) {
            this.specialType = Objects.requireNonNull(specialType, "specialType");
            return this;
        }

        public BingoTag build() {
            return new BingoTag(new FloatImmutableList(difficultyMax), allowedOnSameLine, specialType);
        }
    }

    public enum SpecialType implements StringRepresentable {
        NONE(null), NEVER(0xff5555), FINISH(0x5555ff);

        public static final EnumCodec<SpecialType> CODEC = StringRepresentable.fromEnum(SpecialType::values);
        public static final StreamCodec<FriendlyByteBuf, SpecialType> STREAM_CODEC = BingoStreamCodecs.enum_(SpecialType.class);

        @Nullable
        public final Integer incompleteColor;

        SpecialType(@Nullable Integer incompleteColor) {
            this.incompleteColor = incompleteColor;
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
