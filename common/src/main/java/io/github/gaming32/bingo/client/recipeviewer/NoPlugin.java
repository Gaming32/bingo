package io.github.gaming32.bingo.client.recipeviewer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class NoPlugin extends RecipeViewerPlugin {
    @Override
    public boolean isViewRecipe(Minecraft minecraft) {
        return false;
    }

    @Override
    public boolean isViewUsages(Minecraft minecraft) {
        return false;
    }

    @Override
    public void showRecipe(ItemStack stack) {
    }

    @Override
    public void showUsages(ItemStack stack) {
    }
}
