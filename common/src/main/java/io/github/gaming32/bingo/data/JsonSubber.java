package io.github.gaming32.bingo.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.GsonHelper;

public record JsonSubber(JsonElement json) {
    public JsonSubber sub(String path, String key) {
        return sub(path, new BingoSub.SubBingoSub(key));
    }

    public JsonSubber sub(String path, BingoSub sub) {
        JsonElement element = json;
        String[] parts = path.split("\\.");
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (element.isJsonObject()) {
                element = element.getAsJsonObject().get(part);
                if (element == null) {
                    throw new IllegalArgumentException("Could not find member \"" + part + "\" in json object");
                }
            } else if (element.isJsonArray()) {
                int index;
                try {
                    index = Integer.parseInt(part);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid index \"" + part + "\" into json array");
                }
                JsonArray array = element.getAsJsonArray();
                if (index < 0 || index >= array.size()) {
                    throw new IllegalArgumentException("Index " + index + " is out of bounds for json array with length " + array.size());
                }
                element = array.get(index);
            } else {
                throw new IllegalArgumentException("Could not find member \"" + part + "\" in json element of type " + GsonHelper.getType(element));
            }
        }

        String finalPart = parts[parts.length - 1];
        if (element.isJsonObject()) {
            element.getAsJsonObject().add(finalPart, sub.serializeWithType("bingo_type"));
        } else if (element.isJsonArray()) {
            int index;
            try {
                index = Integer.parseInt(finalPart);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid index \"" + finalPart + "\" into json array");
            }
            JsonArray array = element.getAsJsonArray();
            if (index < 0 || index >= array.size()) {
                throw new IllegalArgumentException("Index " + index + " is out of bounds for json array with length " + array.size());
            }
            array.set(index, sub.serializeWithType("bingo_type"));
        } else {
            throw new IllegalArgumentException("Could not find member \"" + finalPart + "\" in json element of type " + GsonHelper.getType(element));
        }

        return this;
    }
}
