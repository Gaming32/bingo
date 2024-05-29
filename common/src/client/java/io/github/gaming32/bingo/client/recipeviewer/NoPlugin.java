package io.github.gaming32.bingo.client.recipeviewer;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.ItemStack;

public class NoPlugin extends RecipeViewerPlugin {
    @Override
    public boolean isViewRecipe(InputConstants.Key key) {
        return false;
    }

    @Override
    public boolean isViewUsages(InputConstants.Key key) {
        return false;
    }

    @Override
    public void showRecipe(ItemStack stack) {
    }

    @Override
    public void showUsages(ItemStack stack) {
    }
}
