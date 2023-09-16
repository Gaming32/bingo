package io.github.gaming32.bingo.data;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multisets;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

public interface BingoSub {
    Map<String, Function<JsonObject, BingoSub>> SUBS = Map.of(
        "sub", SubBingoSub::new,
        "random", RandomBingoSub::new,
        "int", IntBingoSub::new,
        "wrap", WrapBingoSub::new,
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

    static BingoSub literal(int value) {
        return new IntBingoSub(ConstantInt.of(value));
    }

    static BingoSub literal(String value) {
        return new WrapBingoSub(new JsonPrimitive(value));
    }

    static BingoSub wrapInArray(BingoSub sub) {
        final JsonArray array = new JsonArray(1);
        array.add(sub.serializeWithType("bingo_type"));
        return new WrapBingoSub(array);
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
            this(Util.getOrThrow(IntProvider.CODEC.parse(
                JsonOps.INSTANCE, GsonHelper.getNonNull(data, "value")
            ), JsonSyntaxException::new));
        }

        @Override
        public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
            return new JsonPrimitive(provider.sample(rand));
        }

        @Override
        public JsonObject serialize() {
            final JsonObject result = new JsonObject();
            result.add("value", Util.getOrThrow(
                IntProvider.CODEC.encodeStart(JsonOps.INSTANCE, provider), IllegalArgumentException::new
            ));
            return result;
        }

        @Override
        public String getType() {
            return "int";
        }
    }

    class WrapBingoSub implements BingoSub {
        private final JsonElement value;

        public WrapBingoSub(JsonElement value) {
            this.value = value;
        }

        public WrapBingoSub(JsonElement value, Consumer<JsonSubber> subber) {
            final JsonSubber useSubber = new JsonSubber(value);
            subber.accept(useSubber);
            this.value = useSubber.json();
        }

        public WrapBingoSub(JsonObject data) {
            value = data.get("value");
            if (value == null) {
                throw new IllegalArgumentException("Missing value");
            }
        }

        @Override
        public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
            return BingoGoal.performSubstitutions(value, referable, rand);
        }

        @Override
        public JsonObject serialize() {
            final JsonObject result = new JsonObject();
            result.add("value", value);
            return result;
        }

        @Override
        public String getType() {
            return "wrap";
        }
    }

    class CompoundBingoSub implements BingoSub {
        private final ElementType elementType;
        private final Operator operator;
        private final List<BingoSub> factors;

        public CompoundBingoSub(ElementType elementType, Operator operator, BingoSub... factors) {
            if (factors.length == 0) {
                throw new IllegalArgumentException("factors.length == 0");
            }
            this.elementType = elementType;
            this.operator = operator;
            this.factors = List.of(factors);
        }

        public CompoundBingoSub(JsonObject data) {
            final String elementTypeName = GsonHelper.getAsString(data, "element_type", "int");
            elementType = ElementType.CODEC.byName(elementTypeName);
            if (elementType == null) {
                throw new JsonSyntaxException("Unknown compound element type: " + elementTypeName);
            }

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
            if (factors.isEmpty()) {
                throw new JsonSyntaxException("factors is empty!");
            }
        }

        @Override
        public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
            final BinaryOperator<JsonElement> op = elementType.accumulator.apply(operator);
            JsonElement result = factors.get(0).substitute(referable, rand);
            for (int i = 1; i < factors.size(); i++) {
                result = op.apply(result, factors.get(i).substitute(referable, rand));
            }
            return result;
        }

        @Override
        public JsonObject serialize() {
            final JsonObject result = new JsonObject();

            result.addProperty("element_type", elementType.getSerializedName());
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

        public enum ElementType implements StringRepresentable {
            INT(op -> switch (op) {
                case SUM -> (a, b) -> new JsonPrimitive(a.getAsInt() + b.getAsInt());
                case MUL -> (a, b) -> new JsonPrimitive(a.getAsInt() * b.getAsInt());
                case SUB -> (a, b) -> new JsonPrimitive(a.getAsInt() - b.getAsInt());
                case DIV -> (a, b) -> new JsonPrimitive(a.getAsInt() / b.getAsInt());
            }),
            DOUBLE(op -> switch (op) {
                case SUM -> (a, b) -> new JsonPrimitive(a.getAsDouble() + b.getAsDouble());
                case MUL -> (a, b) -> new JsonPrimitive(a.getAsDouble() * b.getAsDouble());
                case SUB -> (a, b) -> new JsonPrimitive(a.getAsDouble() - b.getAsDouble());
                case DIV -> (a, b) -> new JsonPrimitive(a.getAsDouble() / b.getAsDouble());
            }),
            STRING(op -> switch (op) {
                case SUM -> (a, b) -> new JsonPrimitive(a.getAsString() + b.getAsString());
                case MUL -> (a, b) -> new JsonPrimitive(a.getAsString().repeat(b.getAsInt()));
                default -> throw new UnsupportedOperationException(op + " on STRING");
            }),
            ARRAY(op -> switch (op) {
                case SUM -> (a, b) -> {
                    final JsonArray aa = a.getAsJsonArray();
                    final JsonArray ba = b.getAsJsonArray();
                    final JsonArray result = new JsonArray(aa.size() + ba.size());
                    result.addAll(aa);
                    result.addAll(ba);
                    return result;
                };
                case MUL -> (a, b) -> {
                    final JsonArray aa = a.getAsJsonArray();
                    final int count = b.getAsInt();
                    if (count < 0) {
                        throw new IllegalArgumentException("count < 0");
                    }
                    final JsonArray result = new JsonArray(aa.size() * count);
                    for (int i = 0; i < count; i++) {
                        result.addAll(aa);
                    }
                    return result;
                };
                case SUB -> (a, b) -> {
                    final JsonArray aa = a.getAsJsonArray();
                    final JsonArray ba = b.getAsJsonArray();
                    final JsonArray result = new JsonArray(aa.size() - ba.size());
                    //noinspection UnstableApiUsage
                    result.asList().addAll(Multisets.difference(
                        ImmutableMultiset.copyOf(aa), ImmutableMultiset.copyOf(ba)
                    ));
                    return result;
                };
                default -> throw new UnsupportedOperationException(op + " on ARRAY");
            });

            @SuppressWarnings("deprecation")
            public static final EnumCodec<ElementType> CODEC = StringRepresentable.fromEnum(ElementType::values);

            public final Function<Operator, BinaryOperator<JsonElement>> accumulator;

            ElementType(Function<Operator, BinaryOperator<JsonElement>> accumulator) {
                this.accumulator = accumulator;
            }

            @NotNull
            @Override
            public String getSerializedName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }

        public enum Operator implements StringRepresentable {
            SUM, MUL, SUB, DIV;

            @SuppressWarnings("deprecation")
            public static final EnumCodec<Operator> CODEC = StringRepresentable.fromEnum(Operator::values);

            @NotNull
            @Override
            public String getSerializedName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }
    }
}
