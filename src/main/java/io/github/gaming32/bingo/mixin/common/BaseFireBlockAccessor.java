package io.github.gaming32.bingo.mixin.common;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseFireBlock.class)
public interface BaseFireBlockAccessor {
    @Invoker
    boolean callCanBurn(BlockState state);
}
