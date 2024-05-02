package io.github.gaming32.bingo.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.util.BingoStreamCodecs;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatImmutableList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private static Map<ResourceLocation, Holder> tags = Map.of();

    @Nullable
    public static Holder getTag(ResourceLocation id) {
        return tags.get(id);
    }

    public static Set<ResourceLocation> getTags() {
        return tags.keySet();
    }

    public float getUnscaledMaxForDifficulty(int difficulty) {
        if (difficulty < 0) {
            throw new IllegalArgumentException("difficulty < 0 is invalid");
        }
        final int size = difficultyMax.size();
        return difficulty < size ? difficultyMax.getFloat(difficulty) : difficultyMax.getFloat(size - 1);
    }

    public int getMaxForDifficulty(int difficulty, int boardSize) {
        return Mth.ceil(getUnscaledMaxForDifficulty(difficulty) * boardSize * boardSize);
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public record Holder(ResourceLocation id, BingoTag tag) {
        @Override
        public String toString() {
            return id.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Holder h && id.equals(h.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    public static final class Builder {
        private final ResourceLocation id;
        private FloatList difficultyMax = new FloatArrayList();
        private boolean allowedOnSameLine = true;
        private SpecialType specialType = SpecialType.NONE;

        private Builder(ResourceLocation id) {
            this.id = id;
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

        public Builder disallowOnSameLine() {
            this.allowedOnSameLine = false;
            return this;
        }

        public Builder specialType(SpecialType specialType) {
            this.specialType = Objects.requireNonNull(specialType, "specialType");
            return this;
        }

        public Holder build() {
            return new Holder(id, new BingoTag(new FloatImmutableList(difficultyMax), allowedOnSameLine, specialType));
        }
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public static final ResourceLocation ID = new ResourceLocation("bingo:tags");
        private static final Gson GSON = new GsonBuilder().create();

        private final HolderLookup.Provider registries;

        public ReloadListener(HolderLookup.Provider registries) {
            super(GSON, "bingo/tags");
            this.registries = registries;
        }

        @NotNull
        @Override
        public String getName() {
            return ID.toString();
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
            final RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
            final ImmutableMap.Builder<ResourceLocation, Holder> result = ImmutableMap.builder();
            for (final var entry : jsons.entrySet()) {
                try {
                    final BingoTag tag = CODEC.parse(ops, entry.getValue()).getOrThrow(JsonParseException::new);
                    final Holder holder = new Holder(entry.getKey(), tag);
                    result.put(holder.id, holder);
                } catch (Exception e) {
                    Bingo.LOGGER.error("Parsing error in bingo tag {}: {}", entry.getKey(), e.getMessage());
                }
            }
            tags = result.build();
            Bingo.LOGGER.info("Loaded {} bingo tags", tags.size());
        }
    }

    public enum SpecialType implements StringRepresentable {
        NONE(null), NEVER(0xff5555), FINISH(0x5555ff);

        @SuppressWarnings("deprecation")
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
