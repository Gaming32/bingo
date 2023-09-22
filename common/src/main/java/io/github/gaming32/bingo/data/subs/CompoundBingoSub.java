package io.github.gaming32.bingo.data.subs;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multisets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public record CompoundBingoSub(ElementType elementType, Operator operator, List<BingoSub> factors) implements BingoSub {
    public static final Codec<List<BingoSub>> FACTORS_CODEC = BingoSub.CODEC.listOf().comapFlatMap(
        list -> !list.isEmpty() ? DataResult.success(list) : DataResult.error(() -> "factors is empty!"),
        Function.identity()
    );
    public static final Codec<CompoundBingoSub> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ElementType.CODEC.optionalFieldOf("element_type", ElementType.INT).forGetter(CompoundBingoSub::elementType),
        Operator.CODEC.fieldOf("operator").forGetter(CompoundBingoSub::operator),
        FACTORS_CODEC.fieldOf("factors").forGetter(CompoundBingoSub::factors)
    ).apply(instance, CompoundBingoSub::new));

    public CompoundBingoSub {
        if (factors.isEmpty()) {
            throw new IllegalArgumentException("factors is empty!");
        }
    }

    public CompoundBingoSub(ElementType elementType, Operator operator, BingoSub... factors) {
        this(elementType, operator, List.of(factors));
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
    public BingoSubType<CompoundBingoSub> getType() {
        return BingoSubType.COMPOUND.get();
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
