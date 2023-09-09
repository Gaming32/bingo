package io.github.gaming32.bingo.data;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;

public interface BingoSub {
    Map<String, Function<JsonObject, BingoSub>> SUBS = Map.of(
        "sub", SubBingoSub::new,
        "random", RandomBingoSub::new,
        "int", IntBingoSub::new,
        "compound", CompoundBingoSub::new
    );

    JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand);

    JsonObject serialize();

    String getType();

    default JsonObject serializeWithType(String typeKey) {
        JsonObject object = serialize();
        // add type first
        JsonObject newObject = new JsonObject();
        newObject.addProperty(typeKey, getType());
        newObject.asMap().putAll(object.asMap());
        return newObject;
    }

    static BingoSub deserialize(JsonElement element, String typeKey) {
        final JsonObject data = GsonHelper.convertToJsonObject(element, "bingo sub");
        final String type = GsonHelper.getAsString(data, typeKey);
        final Function<JsonObject, BingoSub> factory = SUBS.get(type);
        if (factory == null) {
            throw new JsonSyntaxException("Bingo sub type not found: " + type);
        }
        return factory.apply(data);
    }

    static BingoSub random(int min, int max) {
        return new IntBingoSub(UniformInt.of(min, max));
    }

    // TODO: Remove redundant "BingoSub" suffix
    class SubBingoSub implements BingoSub {
        private final String key;

        public SubBingoSub(String key) {
            this.key = key;
        }

        public SubBingoSub(JsonObject data) {
            key = GsonHelper.getAsString(data, "id");
        }

        @Override
        public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
            final JsonElement value = referable.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Unresolved reference in bingo goal: " + key);
            }
            return value;
        }

        @Override
        public JsonObject serialize() {
            final JsonObject result = new JsonObject();
            result.addProperty("id", key);
            return result;
        }

        @Override
        public String getType() {
            return "sub";
        }
    }

    /**
     * @deprecated Use {@link IntBingoSub} with {@link UniformInt} instead.
     */
    @Deprecated
    class RandomBingoSub implements BingoSub {
        private final MinMaxBounds.Ints range;
        private final int min;
        private final int max;

        public RandomBingoSub(MinMaxBounds.Ints range) {
            this.range = range;
            this.min = range.getMin() != null ? range.getMin() : Integer.MIN_VALUE;
            this.max = range.getMax() != null ? range.getMax() : Integer.MAX_VALUE;
        }

        public RandomBingoSub(JsonObject data) {
            this(MinMaxBounds.Ints.fromJson(data.get("range")));
        }

        @Override
        public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
            return new JsonPrimitive(rand.nextIntBetweenInclusive(min, max));
        }

        @Override
        public JsonObject serialize() {
            final JsonObject result = new JsonObject();
            result.add("range", range.serializeToJson());
            return result;
        }

        @Override
        public String getType() {
            return "random";
        }
    }

    class IntBingoSub implements BingoSub {
        private final IntProvider provider;

        public IntBingoSub(IntProvider provider) {
            this.provider = provider;
        }

        public IntBingoSub(JsonObject data) {
            this(IntProvider.CODEC.decode(
                JsonOps.INSTANCE, GsonHelper.getNonNull(data, "value")
            ).get().orThrow().getFirst());
        }

        @Override
        public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
            return new JsonPrimitive(provider.sample(rand));
        }

        @Override
        public JsonObject serialize() {
            final JsonObject result = new JsonObject();
            result.add("value", IntProvider.CODEC.encodeStart(JsonOps.INSTANCE, provider).get().orThrow());
            return result;
        }

        @Override
        public String getType() {
            return "int";
        }
    }

    class CompoundBingoSub implements BingoSub {
        private final Operator operator;
        private final List<BingoSub> factors;

        public CompoundBingoSub(Operator operator, BingoSub... factors) {
            this.operator = operator;
            this.factors = List.of(factors);
        }

        public CompoundBingoSub(JsonObject data) {
            final String operatorName = GsonHelper.getAsString(data, "operator");
            operator = Operator.CODEC.byName(operatorName);
            if (operator == null) {
                throw new JsonSyntaxException("Unknown compound operator: " + operatorName);
            }

            factors = GsonHelper.getAsJsonArray(data, "factors")
                .asList()
                .stream()
                .map(e -> BingoSub.deserialize(e, "type"))
                .toList();
        }

        @Override
        public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
            int result = operator.base;
            for (final BingoSub factor : factors) {
                result = operator.operator.applyAsInt(result, factor.substitute(referable, rand).getAsInt());
            }
            return new JsonPrimitive(result);
        }

        @Override
        public JsonObject serialize() {
            final JsonObject result = new JsonObject();

            result.addProperty("operator", operator.getSerializedName());

            final JsonArray factorsArray = new JsonArray(factors.size());
            for (final BingoSub factor : factors) {
                factorsArray.add(factor.serializeWithType("type"));
            }
            result.add("factors", factorsArray);

            return result;
        }

        @Override
        public String getType() {
            return "compound";
        }

        public enum Operator implements StringRepresentable {
            SUM(0, Integer::sum),
            MULTIPLY(1, (a, b) -> a * b),
            SUB(0, (a, b) -> a - b);

            @SuppressWarnings("deprecation")
            public static final EnumCodec<Operator> CODEC = StringRepresentable.fromEnum(Operator::values);

            public final int base;
            public final IntBinaryOperator operator;

            Operator(int base, IntBinaryOperator operator) {
                this.base = base;
                this.operator = operator;
            }

            @NotNull
            @Override
            public String getSerializedName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }
    }
}
