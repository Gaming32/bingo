package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.ClientIconTooltip;
import io.github.gaming32.bingo.client.IconTooltip;
import io.github.gaming32.bingo.network.BingoNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.network.NetworkRegistry;

import java.util.Set;

@Mod(Bingo.MOD_ID)
public class BingoNeoForge {
    public BingoNeoForge() {
        Bingo.init();

        final String protocolVersion = Integer.toString(BingoNetwork.PROTOCOL_VERSION);
        final Set<String> allowedVersions = Set.of(
            protocolVersion, NetworkRegistry.ACCEPTVANILLA, NetworkRegistry.ABSENT.version()
        );
        NetworkRegistry.newEventChannel(
            BingoNetwork.PROTOCOL_VERSION_PACKET,
            () -> protocolVersion, allowedVersions::contains, allowedVersions::contains
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
