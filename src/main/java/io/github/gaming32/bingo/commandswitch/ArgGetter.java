package io.github.gaming32.bingo.commandswitch;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@FunctionalInterface
public interface ArgGetter<T> {
    T get(CommandContext<CommandSourceStack> context, String arg) throws CommandSyntaxException;

    static <R> ArgGetter<Holder.Reference<R>> forResource(
        ResourceKey<Registry<R>> registry,
        DynamicCommandExceptionType exceptionType
    ) {
        return (context, arg) -> ResourceKeyArgument.resolveKey(context, arg, registry, exceptionType);
    }
}
