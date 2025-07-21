package io.github.gaming32.bingo.commandswitch;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

record ConstantSwitch<T>(String name, T value, T fallback) implements CommandSwitch<T> {
    @Override
    public void addTo(CommandNode<CommandSourceStack> node) {
        node.addChild(literal(name)
            .redirect(node, stack -> ((CommandSourceStackExt) stack.getSource()).bingo$withFlag(this))
            .build()
        );
    }

    @Override
    public T get(CommandContext<CommandSourceStack> context) {
        return ((CommandSourceStackExt) context.getSource()).bingo$hasFlag(this) ? value : fallback;
    }
}
