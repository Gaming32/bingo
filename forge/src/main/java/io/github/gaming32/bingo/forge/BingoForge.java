package io.github.gaming32.bingo.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.ClientIconTooltip;
import io.github.gaming32.bingo.client.IconTooltip;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Bingo.MOD_ID)
public class BingoForge {
    public BingoForge() {
        EventBuses.registerModEventBus(Bingo.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Bingo.init();
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Bingo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerClientTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(IconTooltip.class, ClientIconTooltip::new);
        }
    }
}
