package io.github.gaming32.bingo.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.subs.SubBingoSub;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Modifies json by searching for a path and replacing an element at the destination.
 *
 * <p>
 * The path is in the format {@code foo.*.1.bar}, where {@code foo} and {@code bar} are keys in an object, and {@code 1}
 * is an index in an array. {@code *} is special syntax which means select all children of the current object or array.
 *
 * <p>
 * The last element in the path can take an optional prefix specifying whether or not it's required to exist. Here's
 * the behavior of different prefixes:
 *
 * <table>
 *     <tr><th>Prefix</th><th>If present</th><th>If absent</th></tr>
 *     <tr><td>(none)</td><td>Replaces the element</td><td>Error</td></tr>
 *     <tr><td>{@code +}</td><td>Error for objects, insert before for arrays</td><td>Adds the element with this key for
 *         objects, or inserts before this index for arrays. If it is just a {@code +} on its own in an array, adds to
 *         the end of the array.</td></tr>
 *     <tr><td>{@code ?}</td><td>Replaces the element</td><td>Adds the element with this key for objects, error for
 *         arrays</td></tr>
 * </table>
 */
public record JsonSubber(JsonElement json) {
    public JsonSubber sub(String path, String key) {
        return sub(path, new SubBingoSub(key));
    }

    public JsonSubber sub(String path, BingoSub sub) {
        return sub(path, BingoUtil.toJsonElement(BingoSub.INNER_CODEC, sub));
    }

    public JsonSubber sub(String path, JsonElement newValue) {
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
                    throw new IllegalArgumentException("Could not find " + pathToString(offset) + " in " + value);
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
            return new SubbingElement(Util.copyAndAdd(path, offset), next);
        }

        void resolveAll(String offset, List<SubbingElement> output) {
            if (!offset.equals("*")) {
                output.add(resolve(offset));
                return;
            }
            if (value.isJsonObject()) {
                for (final var entry : value.getAsJsonObject().entrySet()) {
                    output.add(new SubbingElement(Util.copyAndAdd(path, entry.getKey()), entry.getValue()));
                }
            } else if (value.isJsonArray()) {
                final JsonArray array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    output.add(new SubbingElement(Util.copyAndAdd(path, Integer.toString(i)), array.get(i)));
                }
            } else {
                throw notAnArrayOrObject();
            }
        }

        void modify(String offset, JsonElement newValue) {
            final boolean add = offset.startsWith("+");
            final boolean optional = offset.startsWith("?");
            final boolean modify = !add && !optional;
            if (!modify) {
                offset = offset.substring(1);
            }

            if (value.isJsonObject()) {
                final JsonObject obj = value.getAsJsonObject();
                if (obj.has(offset)) {
                    if (add) {
                        throw new IllegalArgumentException("Expected key \"" + offset + "\" to not exist in " + pathToString(null) + ", but was present");
                    }
                } else {
                    if (modify) {
                        throw new IllegalArgumentException("Could not find \"" + offset + "\" in " + pathToString(null));
                    }
                }

                obj.add(offset, newValue);
            } else if (value.isJsonArray()) {
                final JsonArray array = value.getAsJsonArray();

                if (add && offset.isEmpty()) {
                    array.add(newValue);
                    return;
                }

                int index;
                try {
                    index = Integer.parseInt(offset);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid index \"" + offset + "\" into array at " + pathToString(null));
                }
                if (index < 0 || index >= array.size() || (add && index == array.size())) {
                    throw new IllegalArgumentException("Index " + index + " is out of bounds for array with length " + array.size() + " at " + pathToString(null));
                }
                if (add) {
                    array.asList().add(index, newValue);
                } else {
                    array.set(index, newValue);
                }
            } else {
                throw notAnArrayOrObject();
            }
        }

        void modifyAll(String offset, JsonElement newValue) {
            if (!offset.equals("*")) {
                modify(offset, newValue.deepCopy());
                return;
            }
            if (value.isJsonObject()) {
                for (final var entry : value.getAsJsonObject().entrySet()) {
                    entry.setValue(newValue.deepCopy());
                }
            } else if (value.isJsonArray()) {
                final JsonArray array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    array.set(i, newValue.deepCopy());
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
