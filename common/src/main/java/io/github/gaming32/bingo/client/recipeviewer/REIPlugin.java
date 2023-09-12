package io.github.gaming32.bingo.client.recipeviewer;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;

public class REIPlugin extends RecipeViewerPlugin {

    @Override
    public boolean isViewRecipe(InputConstants.Key key) {
        return isKey(ConfigObject.getInstance().getRecipeKeybind(), key, InputConstants.MOUSE_BUTTON_LEFT);
    }

    @Override
    public boolean isViewUsages(InputConstants.Key key) {
        return isKey(ConfigObject.getInstance().getUsageKeybind(), key, InputConstants.MOUSE_BUTTON_RIGHT);
    }

    private boolean isKey(ModifierKeyCode reiKey, InputConstants.Key mcKey, int mouseOverride) {
        return switch (mcKey.getType()) {
            case KEYSYM -> reiKey.matchesKey(mcKey.getValue(), -1);
            case SCANCODE -> reiKey.matchesKey(-1, mcKey.getValue());
            case MOUSE -> reiKey.matchesMouse(mcKey.getValue()) ||
                (reiKey.getType() != InputConstants.Type.MOUSE && mcKey.getValue() == mouseOverride);
        };
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
