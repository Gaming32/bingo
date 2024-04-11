package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BlockPattern;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class BlockPatternCondition implements LootItemCondition {
    public static final Codec<BlockPatternCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.listOf().listOf().fieldOf("aisles").forGetter(BlockPatternCondition::aisles),
            Codec.unboundedMap(BingoCodecs.CHAR, LocationPredicate.CODEC).fieldOf("where").forGetter(BlockPatternCondition::where),
            ExtraCodecs.strictOptionalField(BlockPattern.Rotations.CODEC, "rotations", BlockPattern.Rotations.HORIZONTAL).forGetter(BlockPatternCondition::rotations)
        ).apply(instance, BlockPatternCondition::new)
    );

    private final List<List<String>> aisles;
    private final Map<Character, LocationPredicate> where;
    private final BlockPattern.Rotations rotations;
    private final BlockPattern blockPattern;

    public BlockPatternCondition(
        List<List<String>> aisles,
        Map<Character, LocationPredicate> where,
        BlockPattern.Rotations rotations
    ) {
        this.aisles = aisles;
        this.where = where;
        this.rotations = rotations;

        this.blockPattern = buildBlockPattern(aisles, where);
    }

    @SuppressWarnings("unchecked")
    private static BlockPattern buildBlockPattern(List<List<String>> patternChars, Map<Character, LocationPredicate> where) {
        Map<Character, Predicate<BlockInWorld>> predicates = where.entrySet().stream()
            .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), new BlockPredicateAdapter(entry.getValue())))
            .collect(Util.toMap());
        return new BlockPattern(patternChars.stream().map(aisle -> {
            Predicate<BlockInWorld>[][] aislePredicates = aisle.stream().map(row -> row.chars().mapToObj(ch -> {
                Predicate<BlockInWorld> predicate = ch == ' ' ? blockInWorld -> true : predicates.get((char) ch);
                if (predicate == null) {
                    throw new IllegalStateException("Block pattern uses undefined char '" + (char) ch + "'");
                }
                return predicate;
            }).toArray(Predicate[]::new)).toArray(Predicate[][]::new);
            ArrayUtils.reverse(aislePredicates); // invert y
            return aislePredicates;
        }).toArray(Predicate[][][]::new));
    }

    @Override
    @NotNull
    public LootItemConditionType getType() {
        return BingoConditions.BLOCK_PATTERN.get();
    }

    public List<List<String>> aisles() {
        return aisles;
    }

    public Map<Character, LocationPredicate> where() {
        return where;
    }

    public BlockPattern.Rotations rotations() {
        return rotations;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerLevel level = lootContext.getLevel();
        BlockPos origin = BlockPos.containing(lootContext.getParam(LootContextParams.ORIGIN));
        return blockPattern.find(level, origin, rotations);
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    public static Builder builder() {
        return new Builder();
    }

    record BlockPredicateAdapter(LocationPredicate predicate) implements Predicate<BlockInWorld> {
        @Override
        public boolean test(BlockInWorld blockInWorld) {
            if (!(blockInWorld.getLevel() instanceof ServerLevel level)) {
                return false;
            }
            return this.predicate.matches(level, blockInWorld.getPos().getX() + 0.5, blockInWorld.getPos().getY() + 0.5, blockInWorld.getPos().getZ() + 0.5);
        }
    }

    public static final class Builder implements LootItemCondition.Builder {
        private final List<List<String>> aisles = new ArrayList<>();
        private final Map<Character, LocationPredicate> predicatesByChar = new LinkedHashMap<>();
        private BlockPattern.Rotations rotations = BlockPattern.Rotations.HORIZONTAL;

        private Builder() {
        }

        public Builder aisle(String... aisle) {
            this.aisles.add(List.of(aisle));
            return this;
        }

        public Builder where(char symbol, LocationPredicate predicate) {
            this.predicatesByChar.put(symbol, predicate);
            return this;
        }

        public Builder where(char symbol, BlockPredicate.Builder block) {
            return where(symbol, LocationPredicate.Builder.location().setBlock(block).build());
        }

        public Builder rotations(BlockPattern.Rotations rotations) {
            this.rotations = rotations;
            return this;
        }

        @Override
        @NotNull
        public BlockPatternCondition build() {
            return new BlockPatternCondition(List.copyOf(aisles), predicatesByChar, rotations);
        }
    }
}
