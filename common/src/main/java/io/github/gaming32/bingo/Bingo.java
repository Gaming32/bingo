package io.github.gaming32.bingo;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.logging.LogUtils;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.conditions.BingoConditions;
import io.github.gaming32.bingo.conditions.BingoParamSets;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.icons.GoalIconType;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTrackerType;
import io.github.gaming32.bingo.data.subs.BingoSubType;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.mixin.common.ExplosionAccessor;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.messages.c2s.KeyPressedPacket;
import io.github.gaming32.bingo.network.messages.s2c.InitBoardPacket;
import io.github.gaming32.bingo.network.messages.s2c.RemoveBoardPacket;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPacket;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPacket;
import io.github.gaming32.bingo.network.messages.s2c.UpdateProgressPacket;
import io.github.gaming32.bingo.network.messages.s2c.UpdateStatePacket;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.event.Event;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bingo {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final LevelResource PERSISTED_BINGO_GAME = new LevelResource("persisted_bingo_game.dat");

    public static boolean showOtherTeam;

    public static BingoGame activeGame;
    public static final Set<UUID> needAdvancementsClear = new HashSet<>();

    public static void init() {
        Event.REGISTER_COMMANDS.register(BingoCommand::register);

        Event.PLAYER_JOIN.register(player -> {
            if (activeGame != null) {
                activeGame.addPlayer(player);
            }
        });

        Event.PLAYER_QUIT.register(player -> needAdvancementsClear.remove(player.getUUID()));

        Event.SERVER_STOPPED.register(instance -> activeGame = null);

        Event.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                BingoTriggers.TRY_USE_ITEM.get().trigger(serverPlayer, hand);
            }
        });

        Event.EXPLOSION_START.register((level, explosion) -> {
            if (level instanceof ServerLevel serverLevel) {
                final ServerPlayer player;
                if (explosion.getIndirectSourceEntity() instanceof ServerPlayer thePlayer) {
                    player = thePlayer;
                } else if (((ExplosionAccessor)explosion).getDamageSource().getEntity() instanceof ServerPlayer thePlayer) {
                    player = thePlayer;
                } else {
                    player = null;
                }
                if (player != null) {
                    BingoTriggers.EXPLOSION.get().trigger(player, serverLevel, explosion);
                }
            }
        });

        Event.SERVER_TICK_END.register(instance -> {
            if (activeGame != null && activeGame.isRequireClient()) {
                for (final ServerPlayer player : instance.getPlayerList().getPlayers()) {
                    if (player.tickCount == 60 && !Bingo.isInstalledOnClient(player)) {
                        player.connection.disconnect(BingoGame.REQUIRED_CLIENT_KICK);
                    }
                }
            }
        });

        Event.SERVER_STARTED.register(instance -> {
            final Path path = instance.getWorldPath(PERSISTED_BINGO_GAME);
            if (!Files.isRegularFile(path)) return;
            LOGGER.info("Reading persisted Bingo game");
            try {
                final CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
                final BingoGame.PersistenceData data = BingoUtil.fromTag(BingoGame.PersistenceData.CODEC, tag);
                activeGame = data.createGame(instance.getScoreboard());
                Files.deleteIfExists(path);
            } catch (Exception e) {
                LOGGER.error("Failed to load persisted Bingo game", e);
            }
        });

        Event.SERVER_STOPPING.register(instance -> {
            if (activeGame == null || !activeGame.isPersistent()) return;
            LOGGER.info("Storing persistent Bingo game");
            final Path path = instance.getWorldPath(PERSISTED_BINGO_GAME);
            try {
                final BingoGame.PersistenceData data = activeGame.createPersistenceData();
                final CompoundTag tag = BingoUtil.toCompoundTag(BingoGame.PersistenceData.CODEC, data);
                NbtIo.writeCompressed(tag, path);
            } catch (Exception e) {
                LOGGER.error("Failed to store persistent Bingo game", e);
            }
        });

        BingoConditions.load();
        BingoParamSets.load();
        GoalIconType.load();
        BingoSubType.load();
        ProgressTrackerType.load();
        BingoTriggers.load();

        BingoPlatform.platform.registerDataReloadListeners(registrar -> {
            registrar.register(BingoTag.ReloadListener.ID, new BingoTag.ReloadListener());
            registrar.register(BingoDifficulty.ReloadListener.ID, new BingoDifficulty.ReloadListener());
            registrar.register(
                BingoGoal.ReloadListener.ID, new BingoGoal.ReloadListener(),
                List.of(BingoTag.ReloadListener.ID, BingoDifficulty.ReloadListener.ID)
            );
        });

        BingoNetworking.instance().onRegister(registrar -> {
            registrar.register(PacketFlow.CLIENTBOUND, InitBoardPacket.ID, InitBoardPacket::new);
            registrar.register(PacketFlow.CLIENTBOUND, RemoveBoardPacket.ID, buf -> RemoveBoardPacket.INSTANCE);
            registrar.register(PacketFlow.CLIENTBOUND, ResyncStatesPacket.ID, ResyncStatesPacket::new);
            registrar.register(PacketFlow.CLIENTBOUND, SyncTeamPacket.ID, SyncTeamPacket::new);
            registrar.register(PacketFlow.CLIENTBOUND, UpdateProgressPacket.ID, UpdateProgressPacket::new);
            registrar.register(PacketFlow.CLIENTBOUND, UpdateStatePacket.ID, UpdateStatePacket::new);

            registrar.register(PacketFlow.SERVERBOUND, KeyPressedPacket.ID, KeyPressedPacket::new);
        });

        if (BingoPlatform.platform.isClient()) {
            BingoClient.init();
        }

        LOGGER.info("I got the diagonal!");
    }

    public static void updateCommandTree(PlayerList playerList) {
        playerList.getPlayers().forEach(playerList.getServer().getCommands()::sendCommands);
    }

    public static MutableComponent translatable(@Translatable(allowArbitraryArgs = true) String key, Object... args) {
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
        return BingoNetworking.instance().canPlayerReceive(player, InitBoardPacket.ID);
    }
}
