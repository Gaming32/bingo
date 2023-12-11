package io.github.gaming32.bingo;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.logging.LogUtils;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.registries.RegistrarManager;
import io.github.gaming32.bingo.conditions.BingoConditions;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.icons.GoalIconType;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTrackerType;
import io.github.gaming32.bingo.data.subs.BingoSubType;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.network.BingoNetwork;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.players.PlayerList;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bingo {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final RegistrarManager REGISTRAR_MANAGER = RegistrarManager.get(MOD_ID);

    public static boolean showOtherTeam;

    public static BingoGame activeGame;
    public static final Set<UUID> needAdvancementsClear = new HashSet<>();

    public static void init() {
        CommandRegistrationEvent.EVENT.register(BingoCommand::register);

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (activeGame != null) {
                activeGame.addPlayer(player);
            }
        });

        PlayerEvent.PLAYER_QUIT.register(player -> needAdvancementsClear.remove(player.getUUID()));

        LifecycleEvent.SERVER_STOPPED.register(instance -> activeGame = null);

        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                BingoTriggers.TRY_USE_ITEM.get().trigger(serverPlayer, hand);
            }
            return CompoundEventResult.pass();
        });

        TickEvent.SERVER_POST.register(instance -> {
            if (activeGame != null && activeGame.isRequireClient()) {
                for (final ServerPlayer player : instance.getPlayerList().getPlayers()) {
                    if (player.tickCount == 60 && !Bingo.isInstalledOnClient(player)) {
                        player.connection.disconnect(BingoGame.REQUIRED_CLIENT_KICK);
                    }
                }
            }
        });

        BingoConditions.load();
        GoalIconType.load();
        BingoSubType.load();
        ProgressTrackerType.load();
        BingoTriggers.load();

        ReloadListenerRegistry.register(
            PackType.SERVER_DATA,
            new BingoTag.ReloadListener(),
            BingoTag.ReloadListener.ID
        );
        ReloadListenerRegistry.register(
            PackType.SERVER_DATA,
            new BingoDifficulty.ReloadListener(),
            BingoDifficulty.ReloadListener.ID
        );
        ReloadListenerRegistry.register(
            PackType.SERVER_DATA,
            new BingoGoal.ReloadListener(),
            BingoGoal.ReloadListener.ID,
            List.of(BingoTag.ReloadListener.ID, BingoDifficulty.ReloadListener.ID)
        );

        BingoNetwork.load();

        LOGGER.info("I got the diagonal!");
    }

    public static void updateCommandTree(PlayerList playerList) {
        playerList.getPlayers().forEach(playerList.getServer().getCommands()::sendCommands);
    }

    public static MutableComponent translatable(@Translatable String key, Object... args) {
        return ensureHasFallback(Component.translatableEscape(key, args));
    }

    public static MutableComponent ensureHasFallback(MutableComponent component) {
        if (component.getContents() instanceof TranslatableContents translatable && translatable.getFallback() == null) {
            final String fallbackText = Language.getInstance().getOrDefault(translatable.getKey(), null);

            Object[] args = translatable.getArgs();
            if (args.length > 0) {
                args = args.clone();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof MutableComponent subComponent) {
                        args[i] = ensureHasFallback(subComponent);
                    }
                }
            }

            Style style = component.getStyle();
            if (style.getHoverEvent() != null) {
                if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
                    final Component hoverText = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
                    if (hoverText instanceof MutableComponent mutableComponent) {
                        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableComponent));
                    }
                } else if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_ENTITY) {
                    final HoverEvent.EntityTooltipInfo info = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ENTITY);
                    assert info != null;
                    if (info.name.orElse(null) instanceof MutableComponent mutableComponent) {
                        style = style.withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_ENTITY,
                            new HoverEvent.EntityTooltipInfo(info.type, info.id, ensureHasFallback(mutableComponent))
                        ));
                    }
                }
            }

            List<Component> siblings = component.getSiblings();
            if (!siblings.isEmpty()) {
                siblings = new ArrayList<>(siblings);
                for (int i = 0; i < siblings.size(); i++) {
                    if (siblings.get(i) instanceof MutableComponent subComponent) {
                        siblings.set(i, ensureHasFallback(subComponent));
                    }
                }
            }

            final MutableComponent result = Component.translatableWithFallback(
                translatable.getKey(), fallbackText, args
            ).setStyle(style);
            result.getSiblings().addAll(siblings);
            return result;
        }
        return component;
    }

    public static boolean isInstalledOnClient(ServerPlayer player) {
        return NetworkManager.canPlayerReceive(player, BingoNetwork.PROTOCOL_VERSION_PACKET);
    }
}
