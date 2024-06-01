package io.github.gaming32.bingo.util;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.resources.ResourceLocation;

public final class ResourceLocations {
    private ResourceLocations() {
    }

    public static ResourceLocation bingo(String path) {
        return new ResourceLocation(Bingo.MOD_ID, path);
    }

    public static ResourceLocation minecraft(String path) {
        return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, path);
    }
}
