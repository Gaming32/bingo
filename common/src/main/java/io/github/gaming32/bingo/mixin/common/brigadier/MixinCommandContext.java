package io.github.gaming32.bingo.mixin.common.brigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import io.github.gaming32.bingo.ext.CommandContextExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(CommandContext.class)
public class MixinCommandContext<S> implements CommandContextExt {
    @Shadow @Final private Map<String, ParsedArgument<S, ?>> arguments;

    @Override
    public boolean bingo$hasArg(String arg) {
        return arguments.containsKey(arg);
    }
}
