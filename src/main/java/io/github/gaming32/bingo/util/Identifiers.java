package io.github.gaming32.bingo.util;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.resources.Identifier;

public final class Identifiers {
    // Using a prototype caches the validity check on the namespace
    private static final Identifier BINGO_PROTOTYPE = Identifier.fromNamespaceAndPath(Bingo.MOD_ID, "");
    private static final Identifier C_PROTOTYPE = Identifier.fromNamespaceAndPath("c", "");

    private Identifiers() {
    }

    public static Identifier bingo(String path) {
        return BINGO_PROTOTYPE.withPath(path);
    }

    public static Identifier c(String path) {
        return C_PROTOTYPE.withPath(path);
    }

    public static Identifier minecraft(String path) {
        return Identifier.withDefaultNamespace(path);
    }
}
