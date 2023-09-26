//package io.github.gaming32.bingo.client.recipeviewer;
//
//import com.mojang.blaze3d.platform.InputConstants;
//import dev.emi.emi.api.EmiApi;
//import dev.emi.emi.api.stack.EmiStack;
//import dev.emi.emi.config.EmiConfig;
//import dev.emi.emi.input.EmiBind;
//import net.minecraft.world.item.ItemStack;
//
//public class EMIPlugin extends RecipeViewerPlugin {
//    @Override
//    public boolean isViewRecipe(InputConstants.Key key) {
//        return isKey(EmiConfig.viewRecipes, key);
//    }
//
//    @Override
//    public boolean isViewUsages(InputConstants.Key key) {
//        return isKey(EmiConfig.viewUses, key);
//    }
//
//    private boolean isKey(EmiBind emiKey, InputConstants.Key mcKey) {
//        return switch (mcKey.getType()) {
//            case KEYSYM -> emiKey.matchesKey(mcKey.getValue(), -1);
//            case SCANCODE -> emiKey.matchesKey(-1, mcKey.getValue());
//            case MOUSE -> emiKey.matchesMouse(mcKey.getValue());
//        };
//    }
//
//    @Override
//    public void showRecipe(ItemStack stack) {
//        EmiApi.displayRecipes(EmiStack.of(stack));
//    }
//
//    @Override
//    public void showUsages(ItemStack stack) {
//        EmiApi.displayUses(EmiStack.of(stack));
//    }
//}
