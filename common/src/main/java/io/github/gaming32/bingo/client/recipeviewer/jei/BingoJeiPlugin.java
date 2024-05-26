package io.github.gaming32.bingo.client.recipeviewer.jei;

import io.github.gaming32.bingo.util.ResourceLocations;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class BingoJeiPlugin implements IModPlugin {
    private static BingoJeiPlugin instance;
    public IJeiRuntime runtime;

    public BingoJeiPlugin() {
        instance = this;
    }

    public static BingoJeiPlugin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("JEIPlugin.InternalPlugin accessed too early (or late)!");
        }
        return instance;
    }

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocations.bingo("jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }
}
