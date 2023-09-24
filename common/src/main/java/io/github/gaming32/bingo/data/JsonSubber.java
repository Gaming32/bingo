package io.github.gaming32.bingo.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.subs.SubBingoSub;
import io.github.gaming32.bingo.util.Util;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public record JsonSubber(JsonElement json) {
    public JsonSubber sub(String path, String key) {
        return sub(path, new SubBingoSub(key));
    }

    public JsonSubber sub(String path, BingoSub sub) {
        return sub(path, sub.serializeInnerToJson());
    }

    public JsonSubber sub(String path, JsonElement newValue) {
        SubbingElement current = new SubbingElement(List.of(), json);
        final String[] parts = path.split("\\.");

        for (int i = 0; i < parts.length - 1; i++) {
            current = current.resolve(parts[i]);
        }

        current.modify(parts[parts.length - 1], newValue);

        return this;
    }

    public JsonSubber multiSub(String path, String key) {
        return multiSub(path, new SubBingoSub(key));
    }

    public JsonSubber multiSub(String path, BingoSub sub) {
        return multiSub(path, sub.serializeInnerToJson());
    }

    public JsonSubber multiSub(String path, JsonElement newValue) {
        final List<SubbingElement> current = new ArrayList<>(List.of(new SubbingElement(List.of(), json)));
        final List<SubbingElement> next = new ArrayList<>();
        final String[] parts = path.split("\\.");

        for (int i = 0; i < parts.length - 1; i++) {
            final String part = parts[i];
            for (final SubbingElement element : current) {
                element.resolveAll(part, next);
            }
            current.clear();
            current.addAll(next);
            next.clear();
        }

        final String last = parts[parts.length - 1];
        for (final SubbingElement element : current) {
            element.modifyAll(last, newValue);
        }

        return this;
    }

    private record SubbingElement(List<String> path, JsonElement value) {
        SubbingElement resolve(String offset) {
            final JsonElement next;
            if (value.isJsonObject()) {
                next = value.getAsJsonObject().get(offset);
                if (next == null) {
                    throw new IllegalArgumentException("Could not find " + pathToString(offset));
                }
            } else if (value.isJsonArray()) {
                int index;
                try {
                    index = Integer.parseInt(offset);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid index \"" + offset + "\" into array at " + pathToString(null));
                }
                final JsonArray array = value.getAsJsonArray();
                if (index < 0 || index >= array.size()) {
                    throw new IllegalArgumentException("Index " + index + " is out of bounds for array with length " + array.size() + " at " + pathToString(null));
                }
                next = array.get(index);
            } else {
                throw notAnArrayOrObject();
            }
            return new SubbingElement(Util.addToList(path, offset), next);
        }

        void resolveAll(String offset, List<SubbingElement> output) {
            if (!offset.equals("*")) {
                output.add(resolve(offset));
                return;
            }
            if (value.isJsonObject()) {
                for (final var entry : value.getAsJsonObject().entrySet()) {
                    output.add(new SubbingElement(Util.addToList(path, entry.getKey()), entry.getValue()));
                }
            } else if (value.isJsonArray()) {
                final JsonArray array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    output.add(new SubbingElement(Util.addToList(path, Integer.toString(i)), array.get(i)));
                }
            } else {
                throw notAnArrayOrObject();
            }
        }

        void modify(String offset, JsonElement newValue) {
            if (value.isJsonObject()) {
                value.getAsJsonObject().add(offset, newValue);
            } else if (value.isJsonArray()) {
                int index;
                try {
                    index = Integer.parseInt(offset);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid index \"" + offset + "\" into array at " + pathToString(null));
                }
                final JsonArray array = value.getAsJsonArray();
                if (index < 0 || index >= array.size()) {
                    throw new IllegalArgumentException("Index " + index + " is out of bounds for array with length " + array.size() + " at " + pathToString(null));
                }
                array.set(index, newValue);
            } else {
                throw notAnArrayOrObject();
            }
        }

        void modifyAll(String offset, JsonElement newValue) {
            if (!offset.equals("*")) {
                modify(offset, newValue);
                return;
            }
            if (value.isJsonObject()) {
                for (final var entry : value.getAsJsonObject().entrySet()) {
                    entry.setValue(newValue);
                }
            } else if (value.isJsonArray()) {
                final JsonArray array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    array.set(i, newValue);
                }
            } else {
                throw notAnArrayOrObject();
            }
        }

        private IllegalArgumentException notAnArrayOrObject() {
            return new IllegalArgumentException("Element at " + pathToString(null) + " not an array or an object, but " + GsonHelper.getType(value));
        }

        String pathToString(String extra) {
            final StringBuilder result = new StringBuilder();
            for (final String part : path) {
                if (!result.isEmpty()) {
                    result.append('.');
                }
                result.append(part);
            }
            if (extra != null) {
                if (!result.isEmpty()) {
                    result.append('.');
                }
                result.append(extra);
            }
            return result.toString();
        }
    }
}
