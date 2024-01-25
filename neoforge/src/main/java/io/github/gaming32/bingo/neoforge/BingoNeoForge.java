package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.ClientIconTooltip;
import io.github.gaming32.bingo.client.IconTooltip;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@Mod(Bingo.MOD_ID)
public class BingoNeoForge {
    public BingoNeoForge(IEventBus modEventBus) {
        BingoNetworking.init(new BingoNetworkingImpl(modEventBus));
        Bingo.init();
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Bingo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void setupClient(FMLClientSetupEvent event) {
            BingoClient.init();
        }

        @SubscribeEvent
        public static void registerClientTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(IconTooltip.class, ClientIconTooltip::new);
        }
    }
}
