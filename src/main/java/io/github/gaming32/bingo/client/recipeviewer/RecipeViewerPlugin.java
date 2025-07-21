package io.github.gaming32.bingo.client.recipeviewer;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.gaming32.bingo.client.recipeviewer.jei.JEIPlugin;
import io.github.gaming32.bingo.platform.BingoPlatform;
import net.minecraft.world.item.ItemStack;

public abstract class RecipeViewerPlugin {
    public static RecipeViewerPlugin detect() {
        if (BingoPlatform.platform.isModLoaded("emi")) {
            return new EMIPlugin();
        }
        if (BingoPlatform.platform.isModLoaded("jei")) {
            return new JEIPlugin();
        }
        if (BingoPlatform.platform.isModLoaded("roughlyenoughitems")) {
            return new REIPlugin();
        }
        return new NoPlugin();
    }

    public abstract boolean isViewRecipe(InputConstants.Key key);

    public abstract boolean isViewUsages(InputConstants.Key key);

    public abstract void showRecipe(ItemStack stack);

    public abstract void showUsages(ItemStack stack);
}
