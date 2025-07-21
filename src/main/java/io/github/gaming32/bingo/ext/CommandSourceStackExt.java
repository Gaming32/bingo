package io.github.gaming32.bingo.ext;

import io.github.gaming32.bingo.commandswitch.CommandSwitch;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.Optional;

public interface CommandSourceStackExt {
    default CommandSourceStack bingo$withFlag(CommandSwitch<?> flag) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default boolean bingo$hasFlag(CommandSwitch<?> flag) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default <T> CommandSourceStack bingo$withArgument(CommandSwitch<T> arg, T value) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default <T> Optional<T> bingo$getArgument(CommandSwitch<T> arg) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default <T> CommandSourceStack bingo$withRepeatableArgument(CommandSwitch<T> arg, T value) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default <T> Collection<T> bingo$getRepeatableArgument(CommandSwitch<T> arg) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}
