package io.github.gaming32.bingo.util;

public final class Key<T> {
    private final String name;

    private Key(String name) {
        this.name = name;
    }

    public static <T> Key<T> create(String name) {
        return new Key<>(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
