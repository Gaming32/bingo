package io.github.gaming32.bingo.platform.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@FunctionalInterface
public interface CommandRegistrar {
    void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext registry,
        Commands.CommandSelection selection
    );
}
