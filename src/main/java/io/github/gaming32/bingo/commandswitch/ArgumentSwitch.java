package io.github.gaming32.bingo.commandswitch;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import net.minecraft.commands.CommandSourceStack;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.Function;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

record ArgumentSwitch<A, T>(
    String name,
    String argName,
    ArgumentType<A> type,
    ArgGetter<T> getter,
    Function<CommandContext<CommandSourceStack>, T> defaultValue,
    SuggestionProvider<CommandSourceStack> suggests
) implements CommandSwitch<T> {
    @Override
    public void addTo(CommandNode<CommandSourceStack> node) {
        addTo(node, (source, arg, value) -> ((CommandSourceStackExt) source).bingo$withArgument(arg, value));
    }

    void addTo(
        CommandNode<CommandSourceStack> node,
        TriFunction<CommandSourceStack, ArgumentSwitch<A, T>, T, CommandSourceStack> sourceFunction
    ) {
        node.addChild(literal(name)
            .then(argument(argName, type)
                .suggests(suggests)
                .redirect(node, context -> sourceFunction.apply(context.getSource(), this, getter.get(context, argName)))
            )
            .build()
        );
    }

    @Override
    public T get(CommandContext<CommandSourceStack> context) {
        return ((CommandSourceStackExt) context.getSource())
            .bingo$getArgument(this)
            .orElseGet(() -> defaultValue.apply(context));
    }
}
