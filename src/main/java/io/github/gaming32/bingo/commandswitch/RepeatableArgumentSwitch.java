package io.github.gaming32.bingo.commandswitch;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.function.BiFunction;

record RepeatableArgumentSwitch<A, T, C>(
    ArgumentSwitch<A, T> inner,
    BiFunction<CommandContext<CommandSourceStack>, Collection<T>, C> collectionFunction
) implements CommandSwitch<C> {
    @Override
    public String name() {
        return inner.name();
    }

    @Override
    public void addTo(CommandNode<CommandSourceStack> node) {
        inner.addTo(node, (source, arg, value) -> ((CommandSourceStackExt) source).bingo$withRepeatableArgument(arg, value));
    }

    @Override
    public C get(CommandContext<CommandSourceStack> context) {
        return collectionFunction.apply(context, ((CommandSourceStackExt) context.getSource()).bingo$getRepeatableArgument(inner));
    }
}
