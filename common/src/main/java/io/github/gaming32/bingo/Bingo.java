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
import io.github.gaming32.bingo.subpredicates.entity.BingoEntitySubPredicates;
import io.github.gaming32.bingo.subpredicates.item.BingoItemSubPredicates;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
                for (final ServerPlayer player : new ArrayList<>(instance.getPlayerList().getPlayers())) {
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
                final BingoGame.PersistenceData data = BingoGame.PersistenceData.CODEC.parse(
                    instance.overworld().registryAccess().createSerializationContext(NbtOps.INSTANCE), tag
                ).getOrThrow();
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
                final Tag tag = BingoGame.PersistenceData.CODEC.encodeStart(
                    instance.overworld().registryAccess().createSerializationContext(NbtOps.INSTANCE), data
                ).getOrThrow();
                if (tag instanceof CompoundTag compoundTag) {
                    NbtIo.writeCompressed(compoundTag, path);
                } else {
                    throw new IllegalStateException("Bingo game didn't serialize to CompoundTag");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to store persistent Bingo game", e);
            }
        });

        BingoConditions.load();
        BingoParamSets.load();
        GoalIconType.load();
        BingoSubType.load();
        ProgressTrackerType.load();
        BingoEntitySubPredicates.load();
        BingoItemSubPredicates.load();
        BingoTriggers.load();

        BingoPlatform.platform.registerDataReloadListeners(registrar -> {
            registrar.register(BingoTag.ReloadListener.ID, new BingoTag.ReloadListener(registrar.registryAccess()));
            registrar.register(BingoDifficulty.ReloadListener.ID, new BingoDifficulty.ReloadListener(registrar.registryAccess()));
            registrar.register(
                BingoGoal.ReloadListener.ID, new BingoGoal.ReloadListener(registrar.registryAccess()),
                List.of(BingoTag.ReloadListener.ID, BingoDifficulty.ReloadListener.ID)
            );
        });

        BingoNetworking.instance().onRegister(registrar -> {
            registrar.register(PacketFlow.CLIENTBOUND, InitBoardPacket.TYPE, InitBoardPacket.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, RemoveBoardPacket.TYPE, RemoveBoardPacket.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, ResyncStatesPacket.TYPE, ResyncStatesPacket.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, SyncTeamPacket.TYPE, SyncTeamPacket.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, UpdateProgressPacket.TYPE, UpdateProgressPacket.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, UpdateStatePacket.TYPE, UpdateStatePacket.CODEC);

            registrar.register(PacketFlow.SERVERBOUND, KeyPressedPacket.TYPE, KeyPressedPacket.CODEC);
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
        return BingoUtil.ensureHasFallback(Component.translatableEscape(key, args));
    }

    public static boolean isInstalledOnClient(ServerPlayer player) {
        return BingoNetworking.instance().canPlayerReceive(player, InitBoardPacket.TYPE);
    }
}
