package io.github.gaming32.bingo.network;

import dev.architectury.networking.simple.SimpleNetworkManager;
import net.minecraft.resources.ResourceLocation;

public class BingoNetwork {
    public static final int PROTOCOL_VERSION = 2;
    /*
        // TODO: Use this on the NeoForge end.
        final String protocolVersion = Integer.toString(BingoNetwork.PROTOCOL_VERSION);
        final Set<String> allowedVersions = Set.of(
            protocolVersion, NetworkRegistry.ACCEPTVANILLA, NetworkRegistry.ABSENT.version()
        );
        NetworkRegistry.newEventChannel(
            BingoNetwork.PROTOCOL_VERSION_PACKET,
            () -> protocolVersion, allowedVersions::contains, allowedVersions::contains
        );
     */
    public static final ResourceLocation PROTOCOL_VERSION_PACKET = new ResourceLocation("bingo:protocol_version");

    public static final SimpleNetworkManager NETWORK_MANAGER = SimpleNetworkManager.create("bingo");

    public static void load() {
    }
}
