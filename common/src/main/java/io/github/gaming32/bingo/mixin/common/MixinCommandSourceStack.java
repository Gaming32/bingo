package io.github.gaming32.bingo.mixin.common;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.goal.GoalManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSourceStack.class)
public class MixinCommandSourceStack {
    @Inject(
        method = "suggestRegistryElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/CommandSourceStack;registryAccess()Lnet/minecraft/core/RegistryAccess;"
        ),
        cancellable = true
    )
    private void suggestBingoGoals(
        ResourceKey<? extends Registry<?>> resourceKey,
        SharedSuggestionProvider.ElementSuggestionType registryKey,
        SuggestionsBuilder builder,
        CommandContext<?> context,
        CallbackInfoReturnable<CompletableFuture<Suggestions>> cir
    ) {
        if (resourceKey == BingoRegistries.GOAL) {
            cir.setReturnValue(SharedSuggestionProvider.suggestResource(GoalManager.getGoalIds(), builder));
        }
    }
}
