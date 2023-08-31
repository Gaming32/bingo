package io.github.gaming32.bingo.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.gaming32.bingo.Bingo;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Bingo.MOD_ID)
public class BingoForge {
    public BingoForge() {
        EventBuses.registerModEventBus(Bingo.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Bingo.init();
    }
}