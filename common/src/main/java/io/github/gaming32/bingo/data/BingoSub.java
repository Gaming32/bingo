package io.github.gaming32.bingo.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.function.Function;

public interface BingoSub {
    Map<String, Function<JsonObject, BingoSub>> SUBS = Map.of(
        "sub", BingoSub.SubBingoSub::new,
        "random", BingoSub.RandomBingoSub::new
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
}
