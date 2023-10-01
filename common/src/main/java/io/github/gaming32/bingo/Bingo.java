package io.github.gaming32.bingo;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.logging.LogUtils;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.utils.Env;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.conditions.BingoConditions;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.icons.GoalIconType;
import io.github.gaming32.bingo.data.subs.BingoSubType;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.network.BingoNetwork;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.players.PlayerList;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bingo {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final RegistrarManager REGISTRAR_MANAGER = RegistrarManager.get(MOD_ID);

    public static boolean showOtherTeam;

    public static BingoGame activeGame;
    public static final Set<ServerPlayer> needAdvancementsClear = new HashSet<>();

    public static void init() {
        CommandRegistrationEvent.EVENT.register(BingoCommand::register);

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (activeGame != null) {
                activeGame.addPlayer(player);
            }
        });

        PlayerEvent.PLAYER_QUIT.register(needAdvancementsClear::remove);

        LifecycleEvent.SERVER_STOPPED.register(instance -> activeGame = null);

        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                BingoTriggers.TRY_USE_ITEM.trigger(serverPlayer, hand);
            }
            return CompoundEventResult.pass();
        });

        BingoConditions.load();
        GoalIconType.load();
        BingoSubType.load();
        BingoTriggers.load();

        ReloadListenerRegistry.register(
            PackType.SERVER_DATA,
            new BingoTag.ReloadListener(),
            BingoTag.ReloadListener.ID
        );
        ReloadListenerRegistry.register(
            PackType.SERVER_DATA,
            new BingoGoal.ReloadListener(),
            BingoGoal.ReloadListener.ID,
            List.of(BingoTag.ReloadListener.ID)
        );

        BingoNetwork.load();

        if (Platform.getEnvironment() == Env.CLIENT) {
            ClientLifecycleEvent.CLIENT_SETUP.register(mc -> BingoClient.init());
        }

        LOGGER.info("I got the diagonal!");
    }

    public static void updateCommandTree(PlayerList playerList) {
        playerList.getPlayers().forEach(playerList.getServer().getCommands()::sendCommands);
    }

    public static MutableComponent translatable(@Translatable String key, Object... args) {
        return ensureHasFallback(Component.translatable(key, args));
    }

    public static MutableComponent ensureHasFallback(MutableComponent component) {
        if (component.getContents() instanceof TranslatableContents translatable && translatable.getFallback() == null) {
            final MutableComponent result = Component.translatableWithFallback(
                translatable.getKey(), component.getString(), translatable.getArgs()
            ).setStyle(component.getStyle());
            result.getSiblings().addAll(component.getSiblings());
            return result;
        }
        return component;
    }

    public static boolean isInstalledOnClient(ServerPlayer player) {
        return NetworkManager.canPlayerReceive(player, BingoNetwork.PROTOCOL_VERSION_PACKET);
    }
}
