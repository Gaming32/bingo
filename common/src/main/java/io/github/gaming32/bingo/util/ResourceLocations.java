package io.github.gaming32.bingo.util;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.resources.ResourceLocation;

public final class ResourceLocations {
    // Using a prototype caches the validity check on the namespace
    private static final ResourceLocation BINGO_PROTOTYPE = ResourceLocation.fromNamespaceAndPath(Bingo.MOD_ID, "");

    private ResourceLocations() {
    }

    public static ResourceLocation bingo(String path) {
        return BINGO_PROTOTYPE.withPath(path);
    }

    public static ResourceLocation minecraft(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
