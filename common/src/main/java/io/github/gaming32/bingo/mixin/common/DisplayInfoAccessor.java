package io.github.gaming32.bingo.mixin.common;

import com.google.gson.JsonObject;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DisplayInfo.class)
public interface DisplayInfoAccessor {
    @Invoker
    static ItemStack invokeGetIcon(JsonObject json) {
        throw new AssertionError();
    }
}
