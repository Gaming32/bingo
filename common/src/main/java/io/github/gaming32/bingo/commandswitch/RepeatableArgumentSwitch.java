package io.github.gaming32.bingo.commandswitch;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
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
        inner.addTo(node, CommandSourceStack::bingo$withRepeatableArgument);
    }

    @Override
    public C get(CommandContext<CommandSourceStack> context) {
        return collectionFunction.apply(context, context.getSource().bingo$getRepeatableArgument(inner));
    }
}
