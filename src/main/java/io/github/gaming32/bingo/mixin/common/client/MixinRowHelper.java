package io.github.gaming32.bingo.mixin.common.client;

import io.github.gaming32.bingo.ext.RowHelperExt;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GridLayout.RowHelper.class)
public class MixinRowHelper implements RowHelperExt {
    @Shadow
    @Final
    private int columns;
    @Shadow
    private int index;

    @Override
    public void bingo$ensureNewLine() {
        index = Mth.roundToward(index, columns);
    }
}
