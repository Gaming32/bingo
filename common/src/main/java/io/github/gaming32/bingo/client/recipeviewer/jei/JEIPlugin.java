package io.github.gaming32.bingo.client.recipeviewer.jei;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import io.github.gaming32.bingo.client.recipeviewer.RecipeViewerPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IJeiKeyMappings;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public class JEIPlugin extends RecipeViewerPlugin {
    private int viewRecipe = 0;
    private int viewUsages = 0;

    public JEIPlugin() {
        ClientRawInputEvent.KEY_PRESSED.register((client, keyCode, scanCode, action, modifiers) -> {
            final InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
            if (action == InputConstants.PRESS) {
                if (matchesKey(key, IJeiKeyMappings::getShowRecipe)) {
                    viewRecipe++;
                }
                if (matchesKey(key, IJeiKeyMappings::getShowUses)) {
                    viewUsages++;
                }
            } else if (action == InputConstants.RELEASE) {
                if (matchesKey(key, IJeiKeyMappings::getShowRecipe)) {
                    viewRecipe--;
                }
                if (matchesKey(key, IJeiKeyMappings::getShowUses)) {
                    viewUsages--;
                }
            }
            return EventResult.pass();
        });
        ClientRawInputEvent.MOUSE_CLICKED_PRE.register((client, button, action, mods) -> {
            final InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
            if (action == InputConstants.PRESS) {
                if (matchesKey(key, IJeiKeyMappings::getShowRecipe)) {
                    viewRecipe++;
                }
                if (matchesKey(key, IJeiKeyMappings::getShowUses)) {
                    viewUsages++;
                }
            } else if (action == InputConstants.RELEASE) {
                if (matchesKey(key, IJeiKeyMappings::getShowRecipe)) {
                    viewRecipe--;
                }
                if (matchesKey(key, IJeiKeyMappings::getShowUses)) {
                    viewUsages--;
                }
            }
            return EventResult.pass();
        });
    }

    @Override
    public boolean isViewRecipe(InputConstants.Key key) {
        return matchesKey(key, IJeiKeyMappings::getShowRecipe);
    }

    @Override
    public boolean isViewUsages(InputConstants.Key key) {
        return matchesKey(key, IJeiKeyMappings::getShowUses);
    }

    private boolean matchesKey(InputConstants.Key key, Function<IJeiKeyMappings, IJeiKeyMapping> getter) {
        final IJeiRuntime runtime = BingoJeiPlugin.getInstance().runtime;
        if (runtime == null) {
            return false;
        }
        return getter.apply(runtime.getKeyMappings()).isActiveAndMatches(key);
    }

    @Override
    public void showRecipe(ItemStack stack) {
        show(stack, List.of(RecipeIngredientRole.OUTPUT));
    }

    @Override
    public void showUsages(ItemStack stack) {
        show(stack, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST));
    }

    private void show(ItemStack stack, List<RecipeIngredientRole> roles) {
        final IJeiRuntime runtime = BingoJeiPlugin.getInstance().runtime;
        if (runtime == null) return;
        final IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        runtime.getRecipesGui().show(
            roles.stream()
                .<IFocus<?>>map(role ->
                    focusFactory.createFocus(role, VanillaTypes.ITEM_STACK, stack)
                )
                .toList()
        );
    }
}
