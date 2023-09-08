package io.github.gaming32.bingo.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import io.github.gaming32.bingo.util.BlockPattern;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class BlockPatternCondition implements LootItemCondition {
    private final String[][] aisles;
    private final Map<Character, LocationPredicate> predicatesByChar;
    private final BlockPattern blockPattern;
    private final BlockPattern.Rotations rotations;

    public BlockPatternCondition(String[][] aisles, Map<Character, LocationPredicate> predicatesByChar, BlockPattern.Rotations rotations) {
        this.aisles = aisles;
        this.predicatesByChar = predicatesByChar;
        this.blockPattern = buildBlockPattern(aisles, predicatesByChar);
        this.rotations = rotations;
    }

    @SuppressWarnings("unchecked")
    private static BlockPattern buildBlockPattern(String[][] patternChars, Map<Character, LocationPredicate> predicatesByChar) {
        Map<Character, Predicate<BlockInWorld>> predicates = predicatesByChar.entrySet().stream()
            .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), new BlockPredicateAdapter(entry.getValue())))
            .collect(Util.toMap());
        return new BlockPattern(Arrays.stream(patternChars).map(aisle -> {
            Predicate<BlockInWorld>[][] aislePredicates = Arrays.stream(aisle).map(row -> row.chars().mapToObj(ch -> {
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
        return BingoConditions.BLOCK_PATTERN;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerLevel level = lootContext.getLevel();
        BlockPos origin = BlockPos.containing(lootContext.getParam(LootContextParams.ORIGIN));
        return blockPattern.find(level, origin, rotations);
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

    public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BlockPatternCondition> {
        @Override
        public void serialize(JsonObject json, BlockPatternCondition value, JsonSerializationContext context) {
            json.add("aisles", context.serialize(value.aisles));
            JsonObject where = new JsonObject();
            value.predicatesByChar.forEach((symbol, predicate) -> where.add(symbol.toString(), predicate.serializeToJson()));
            json.add("where", where);
            if (value.rotations != BlockPattern.Rotations.HORIZONTAL) {
                json.addProperty("rotations", value.rotations.getSerializedName());
            }
        }

        @Override
        @NotNull
        public BlockPatternCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            String[][] aisles = context.deserialize(GsonHelper.getAsJsonArray(json, "aisles"), String[][].class);

            JsonObject where = GsonHelper.getAsJsonObject(json, "where");
            Map<Character, LocationPredicate> predicatesByChar = new LinkedHashMap<>(where.size());
            where.asMap().forEach((symbol, predicate) -> {
                if (symbol.length() != 1) {
                    throw new JsonSyntaxException("Symbol '" + symbol + "' is not a character");
                }
                predicatesByChar.put(symbol.charAt(0), LocationPredicate.fromJson(predicate));
            });

            String rotationsStr = GsonHelper.getAsString(json, "rotations", BlockPattern.Rotations.HORIZONTAL.getSerializedName());
            BlockPattern.Rotations rotations = BlockPattern.Rotations.fromSerializedName(rotationsStr);
            if (rotations == null) {
                throw new JsonSyntaxException("Invalid rotation '" + rotationsStr + "'");
            }

            return new BlockPatternCondition(aisles, predicatesByChar, rotations);
        }
    }

    public static final class Builder implements LootItemCondition.Builder {
        private final List<String[]> aisles = new ArrayList<>();
        private final Map<Character, LocationPredicate> predicatesByChar = new LinkedHashMap<>();
        private BlockPattern.Rotations rotations = BlockPattern.Rotations.HORIZONTAL;

        private Builder() {
        }

        public Builder aisle(String... aisle) {
            this.aisles.add(aisle);
            return this;
        }

        public Builder where(char symbol, LocationPredicate predicate) {
            this.predicatesByChar.put(symbol, predicate);
            return this;
        }

        public Builder where(char symbol, BlockPredicate block) {
            return where(symbol, LocationPredicate.Builder.location().setBlock(block).build());
        }

        public Builder rotations(BlockPattern.Rotations rotations) {
            this.rotations = rotations;
            return this;
        }

        @Override
        @NotNull
        public BlockPatternCondition build() {
            return new BlockPatternCondition(aisles.toArray(String[][]::new), predicatesByChar, rotations);
        }
    }
}
