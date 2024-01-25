package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.ClientIconTooltip;
import io.github.gaming32.bingo.client.IconTooltip;
import io.github.gaming32.bingo.network.BingoNetwork;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;

@Mod(Bingo.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BingoNeoForge {
    public BingoNeoForge() {
        Bingo.init();
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlerEvent event) {
        event.registrar(Bingo.MOD_ID)
            .versioned(Integer.toString(BingoNetwork.PROTOCOL_VERSION))
            .optional()
            .play(
                BingoNetwork.PROTOCOL_VERSION_PACKET,
                buf -> new DiscardedPayload(BingoNetwork.PROTOCOL_VERSION_PACKET),
                IPlayPayloadHandler.noop()
            );
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
