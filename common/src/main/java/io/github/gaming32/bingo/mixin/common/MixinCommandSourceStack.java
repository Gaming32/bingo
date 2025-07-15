package io.github.gaming32.bingo.mixin.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.gaming32.bingo.commandswitch.CommandSwitch;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.goal.GoalManager;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.Util;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSourceStack.class)
public class MixinCommandSourceStack implements CommandSourceStackExt {
    @Shadow @Final private CommandSource source;
    @Shadow @Final private Vec3 worldPosition;
    @Shadow @Final private Vec2 rotation;
    @Shadow @Final private ServerLevel level;
    @Shadow @Final private int permissionLevel;
    @Shadow @Final private String textName;
    @Shadow @Final private Component displayName;
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private @Nullable Entity entity;
    @Shadow @Final private boolean silent;
    @Shadow @Final private CommandResultCallback resultCallback;
    @Shadow @Final private EntityAnchorArgument.Anchor anchor;
    @Shadow @Final private CommandSigningContext signingContext;
    @Shadow @Final private TaskChainer chatMessageChainer;

    @Unique
    private Set<CommandSwitch<?>> bingo$constants = ImmutableSet.of();
    @Unique
    private Map<CommandSwitch<?>, Object> bingo$arguments = ImmutableMap.of();
    @Unique
    private Multimap<CommandSwitch<?>, Object> bingo$repeatableArguments = ImmutableMultimap.of();

    private MixinCommandSourceStack() {
    }

    @Invoker("<init>")
    static CommandSourceStack create(
        CommandSource source,
        Vec3 worldPosition,
        Vec2 rotation,
        ServerLevel level,
        int permissionLevel,
        String textName,
        Component displayName,
        MinecraftServer server,
        @Nullable Entity entity,
        boolean silent,
        CommandResultCallback resultCallback,
        EntityAnchorArgument.Anchor anchor,
        CommandSigningContext signingContext,
        TaskChainer chatMessageChainer
    ) {
        throw new AssertionError();
    }

    @Override
    public CommandSourceStack bingo$withFlag(CommandSwitch<?> flag) {
        final var copy = bingo$copy();
        ((MixinCommandSourceStack)(CommandSourceStackExt)copy).bingo$constants =
            BingoUtil.copyAndAdd(bingo$constants, flag);
        return copy;
    }

    @Override
    public boolean bingo$hasFlag(CommandSwitch<?> flag) {
        return bingo$constants.contains(flag);
    }

    @Override
    public <T> CommandSourceStack bingo$withArgument(CommandSwitch<T> arg, T value) {
        final var copy = bingo$copy();
        ((MixinCommandSourceStack)(CommandSourceStackExt)copy).bingo$arguments =
            Util.copyAndPut(bingo$arguments, arg, value);
        return copy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> bingo$getArgument(CommandSwitch<T> arg) {
        return Optional.ofNullable((T)bingo$arguments.get(arg));
    }

    @Override
    public <T> CommandSourceStack bingo$withRepeatableArgument(CommandSwitch<T> arg, T value) {
        final var copy = bingo$copy();
        ((MixinCommandSourceStack)(CommandSourceStackExt)copy).bingo$repeatableArguments =
            BingoUtil.copyAndPut(bingo$repeatableArguments, arg, value);
        return copy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> bingo$getRepeatableArgument(CommandSwitch<T> arg) {
        return (Collection<T>)bingo$repeatableArguments.get(arg);
    }

    @Unique
    @NotNull
    private CommandSourceStack bingo$copy() {
        return copyExtraFields(create(
            source,
            worldPosition,
            rotation,
            level,
            permissionLevel,
            textName,
            displayName,
            server,
            entity,
            silent,
            resultCallback,
            anchor,
            signingContext,
            chatMessageChainer
        ));
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @ModifyExpressionValue(
        method = "*",
        at = @At(
            value = "NEW",
            target = "net/minecraft/commands/CommandSourceStack"
        ),
        require = 0,
        expect = 0
    )
    private CommandSourceStack copyExtraFields(@NotNull CommandSourceStack newStack) {
        final var ext = (MixinCommandSourceStack)(CommandSourceStackExt)newStack;
        ext.bingo$constants = bingo$constants;
        ext.bingo$arguments = bingo$arguments;
        ext.bingo$repeatableArguments = bingo$repeatableArguments;
        return newStack;
    }

    @Inject(
        method = "suggestRegistryElements",
        at = @At("HEAD"),
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
