package io.github.gaming32.bingo.data.subs;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multisets;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public record CompoundBingoSub(ElementType elementType, Operator operator, List<BingoSub> factors) implements BingoSub {
    public static final MapCodec<CompoundBingoSub> CODEC = RecordCodecBuilder.<CompoundBingoSub>mapCodec(
        instance -> instance.group(
            ElementType.CODEC.optionalFieldOf("element_type", ElementType.INT).forGetter(CompoundBingoSub::elementType),
            Operator.CODEC.fieldOf("operator").forGetter(CompoundBingoSub::operator),
            ExtraCodecs.nonEmptyList(BingoSub.CODEC.listOf()).fieldOf("factors").forGetter(CompoundBingoSub::factors)
        ).apply(instance, CompoundBingoSub::new)
    ).validate(CompoundBingoSub::validate);

    public CompoundBingoSub(ElementType elementType, Operator operator, BingoSub... factors) {
        this(elementType, operator, List.of(factors));
    }

    private DataResult<CompoundBingoSub> validate() {
        if (!elementType.supportedOperators.contains(operator)) {
            return DataResult.error(() -> "operator " + operator + " not supported on element_type " + elementType);
        }
        return DataResult.success(this);
    }

    @Override
    public Dynamic<?> substitute(SubstitutionContext context) {
        final var op = elementType.accumulator.apply(operator);
        Dynamic<?> result = factors.getFirst().substitute(context);
        for (int i = 1; i < factors.size(); i++) {
            result = op.apply(result, factors.get(i).substitute(context));
        }
        return result;
    }

    @Override
    public DataResult<BingoSub> validate(SubstitutionContext context) {
        return factors.stream()
            .map(sub -> sub.validate(context))
            .reduce(DataResult.success(this), BingoUtil::combineError, (a, b) -> a);
    }

    @Override
    public BingoSubType<CompoundBingoSub> type() {
        return BingoSubType.COMPOUND.get();
    }

    public enum ElementType implements StringRepresentable {
        INT(op -> switch (op) {
            case SUM -> (a, b) -> a.createInt(a.asInt(0) + b.asInt(0));
            case MUL -> (a, b) -> a.createInt(a.asInt(0) * b.asInt(1));
            case SUB -> (a, b) -> a.createInt(a.asInt(0) - b.asInt(0));
            case DIV -> (a, b) -> a.createInt(a.asInt(0) / b.asInt(1));
        }),
        DOUBLE(op -> switch (op) {
            case SUM -> (a, b) -> a.createDouble(a.asDouble(0) + b.asDouble(0));
            case MUL -> (a, b) -> a.createDouble(a.asDouble(0) * b.asDouble(1));
            case SUB -> (a, b) -> a.createDouble(a.asDouble(0) - b.asDouble(0));
            case DIV -> (a, b) -> a.createDouble(a.asDouble(0) / b.asDouble(1));
        }),
        STRING(op -> switch (op) {
            case SUM -> (a, b) -> a.createString(a.asString("") + b.asString(""));
            case MUL -> (a, b) -> a.createString(a.asString("").repeat(b.asInt(1)));
            default -> throw new UnsupportedOperationException(op + " on STRING");
        }, Operator.SUM, Operator.MUL),
        ARRAY(op -> switch (op) {
            case SUM -> (a, b) -> a.createList(Stream.concat(a.asStream(), b.asStream()));
            case MUL -> (a, b) -> a.createList(
                Collections.nCopies(b.asInt(1), a.asList(Function.identity()))
                    .stream()
                    .flatMap(List::stream)
            );
            case SUB -> (a, b) -> a.createList(Multisets.difference(
                ImmutableMultiset.copyOf(a.asStream().iterator()),
                ImmutableMultiset.copyOf(b.asStream().iterator())
            ).stream());
            default -> throw new UnsupportedOperationException(op + " on ARRAY");
        }, Operator.SUM, Operator.MUL, Operator.SUB);

        @SuppressWarnings("deprecation")
        public static final EnumCodec<ElementType> CODEC = StringRepresentable.fromEnum(ElementType::values);

        public final Function<Operator, BinaryOperator<Dynamic<?>>> accumulator;
        public final Set<Operator> supportedOperators;

        ElementType(Function<Operator, BinaryOperator<Dynamic<?>>> accumulator) {
            this(accumulator, Operator.values());
        }

        ElementType(Function<Operator, BinaryOperator<Dynamic<?>>> accumulator, Operator... supportedOperators) {
            this.accumulator = accumulator;
            this.supportedOperators = Set.of(supportedOperators);
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
