package io.github.gaming32.bingo.mixin.common;

import com.mojang.brigadier.context.CommandContext;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(CommandSourceStack.class)
public class MixinCommandSourceStack implements CommandSourceStackExt {
    @Unique
    private final List<CommandContext<CommandSourceStack>> bingo$extraContexts = new ArrayList<>();

    @Override
    public List<CommandContext<CommandSourceStack>> bingo$getExtraContexts() {
        return bingo$extraContexts;
    }
}
