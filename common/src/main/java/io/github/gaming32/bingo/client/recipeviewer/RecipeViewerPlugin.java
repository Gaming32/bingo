package io.github.gaming32.bingo.client.recipeviewer;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.platform.Platform;
import io.github.gaming32.bingo.client.recipeviewer.jei.JEIPlugin;
import net.minecraft.world.item.ItemStack;

public abstract class RecipeViewerPlugin {
    public static RecipeViewerPlugin detect() {
        if (Platform.isModLoaded("emi")) {
            return new EMIPlugin();
        }
        if (Platform.isModLoaded("jei")) {
            return new JEIPlugin();
        }
        if (Platform.isModLoaded("roughlyenoughitems")) {
            return new REIPlugin();
        }
        return new NoPlugin();
    }

    public abstract boolean isViewRecipe(InputConstants.Key key);

    public abstract boolean isViewUsages(InputConstants.Key key);

    public abstract void showRecipe(ItemStack stack);

    public abstract void showUsages(ItemStack stack);
}
