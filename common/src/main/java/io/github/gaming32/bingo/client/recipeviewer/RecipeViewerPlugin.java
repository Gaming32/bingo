package io.github.gaming32.bingo.client.recipeviewer;

import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public abstract class RecipeViewerPlugin {
    public static RecipeViewerPlugin detect() {
        if (Platform.isModLoaded("roughlyenoughitems")) {
            return new REIPlugin();
        }
        return new NoPlugin();
    }

    public abstract boolean isViewRecipe(Minecraft minecraft);

    public abstract boolean isViewUsages(Minecraft minecraft);

    public abstract void showRecipe(ItemStack stack);

    public abstract void showUsages(ItemStack stack);
}
