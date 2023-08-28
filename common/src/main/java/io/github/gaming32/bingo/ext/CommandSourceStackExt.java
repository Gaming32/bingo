package io.github.gaming32.bingo.ext;

import com.mojang.brigadier.SingleRedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

public interface CommandSourceStackExt {
    SingleRedirectModifier<CommandSourceStack> COPY_CONTEXT = ctx -> {
        ((CommandSourceStackExt)ctx.getSource()).bingo$getExtraContexts().add(ctx);
        return ctx.getSource();
    };

    List<CommandContext<CommandSourceStack>> bingo$getExtraContexts();
}
