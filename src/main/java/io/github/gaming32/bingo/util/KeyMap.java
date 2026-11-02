package io.github.gaming32.bingo.util;

import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public final class KeyMap {
    private final Map<Key<?>, Object> map = new IdentityHashMap<>();

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T) map.get(key);
    }

    public <T> T getOrThrow(Key<T> key) {
        T value = get(key);
        if (value == null) {
            throw new NoSuchElementException(key.toString());
        }
        return value;
    }

    public <T> void put(Key<T> key, T value) {
        map.put(key, value);
    }

    public void putAll(KeyMap other) {
        map.putAll(other.map);
    }
}
