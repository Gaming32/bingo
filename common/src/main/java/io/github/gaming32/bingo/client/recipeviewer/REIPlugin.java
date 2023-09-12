package io.github.gaming32.bingo.client.recipeviewer;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class REIPlugin extends RecipeViewerPlugin {
    @Override
    public boolean isViewRecipe(Minecraft minecraft) {
        return isPressed(ConfigObject.getInstance().getRecipeKeybind(), 0);
    }

    @Override
    public boolean isViewUsages(Minecraft minecraft) {
        return isPressed(ConfigObject.getInstance().getUsageKeybind(), 1);
    }

    private boolean isPressed(ModifierKeyCode key, int mouseOverride) {
        if (key.matchesCurrentMouse() || key.matchesCurrentKey()) {
            return true;
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            return false;
        }
        return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), mouseOverride) == GLFW.GLFW_PRESS;
    }

    @Override
    public void showRecipe(ItemStack stack) {
        ViewSearchBuilder.builder().addRecipesFor(EntryStacks.of(stack)).open();
    }

    @Override
    public void showUsages(ItemStack stack) {
        ViewSearchBuilder.builder().addUsagesFor(EntryStacks.of(stack)).open();
    }
}
