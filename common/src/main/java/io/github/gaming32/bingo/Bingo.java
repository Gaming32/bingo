package io.github.gaming32.bingo;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.logging.LogUtils;
import io.github.gaming32.bingo.conditions.BingoConditions;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.goal.GoalManager;
import io.github.gaming32.bingo.data.icons.GoalIconType;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTrackerType;
import io.github.gaming32.bingo.data.subs.BingoSubType;
import io.github.gaming32.bingo.ext.ServerPlayerExt;
import io.github.gaming32.bingo.game.mode.BingoGameMode;
import io.github.gaming32.bingo.game.persistence.PersistenceManager;
import io.github.gaming32.bingo.mixin.common.ServerExplosionAccessor;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.messages.configuration.ProtocolVersionConfigurationTask;
import io.github.gaming32.bingo.network.messages.configuration.ProtocolVersionPayload;
import io.github.gaming32.bingo.network.messages.s2c.InitBoardPayload;
import io.github.gaming32.bingo.network.messages.s2c.RemoveBoardPayload;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPayload;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateEndTimePayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateProgressPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateStatePayload;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.event.Event;
import io.github.gaming32.bingo.subpredicates.entity.BingoEntitySubPredicates;
import io.github.gaming32.bingo.subpredicates.item.BingoItemSubPredicates;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.ConnectionProtocol;
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

public class Bingo {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final LevelResource PERSISTED_BINGO_GAME = new LevelResource("persisted_bingo_game.dat");

    public static void init() {
        registerEventHandlers();
        initializeRegistries();
        registerDatapackRegistries();
        registerDataReloadListeners();
        registerPayloadHandlers();

        LOGGER.info("I got the diagonal!");
    }

    private static void registerEventHandlers() {
        Event.REGISTER_COMMANDS.register(BingoCommand::register);

        Event.REGISTER_CONFIGURATION_TASKS.register(registrar -> {
            if (registrar.canSend(ProtocolVersionPayload.TYPE)) {
                registrar.addTask(ProtocolVersionConfigurationTask.INSTANCE);
            }
        });

        Event.PLAYER_JOIN.register(player -> {
            final var game = player.server.bingo$getGame();
            if (game != null) {
                game.addPlayer(player);
            }
        });

        Event.PLAYER_QUIT.register(player -> {
            final var game = player.server.bingo$getGame();
            if (game != null) {
                game.removePlayer(player);
            }
        });

        Event.COPY_PLAYER.register((from, to) -> ((ServerPlayerExt)from).bingo$copyAdvancementsNeedClearingTo(to));

        Event.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                BingoTriggers.TRY_USE_ITEM.get().trigger(serverPlayer, hand);
            }
        });

        Event.SERVER_EXPLOSION_START.register((level, explosion) -> {
            if (level instanceof ServerLevel serverLevel) {
                final ServerPlayer player;
                if (explosion.getIndirectSourceEntity() instanceof ServerPlayer thePlayer) {
                    player = thePlayer;
                } else if (((ServerExplosionAccessor)explosion).getDamageSource().getEntity() instanceof ServerPlayer thePlayer) {
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
            final var game = instance.bingo$getGame();
            if (game != null) {
                game.tick(instance);
            }
        });

        Event.SERVER_STARTED.register(instance -> {
            final Path path = instance.getWorldPath(PERSISTED_BINGO_GAME);
            if (!Files.isRegularFile(path)) return;
            LOGGER.info("Reading persisted Bingo game");
            try {
                final var tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
                instance.bingo$setGame(PersistenceManager.deserialize(instance.registryAccess(), tag, instance.getScoreboard()));
                Files.deleteIfExists(path);
            } catch (Exception e) {
                LOGGER.error("Failed to load persisted Bingo game", e);
            }
        });

        Event.SERVER_STOPPING.register(instance -> {
            final var game = instance.bingo$getGame();
            if (game == null) return;
            LOGGER.info("Storing persistent Bingo game");
            final Path path = instance.getWorldPath(PERSISTED_BINGO_GAME);
            try {
                final var tag = PersistenceManager.serialize(instance.registryAccess(), game);
                NbtIo.writeCompressed(tag, path);
            } catch (Exception e) {
                LOGGER.error("Failed to store persistent Bingo game", e);
            }
        });
    }

    private static void initializeRegistries() {
        BingoGameMode.load();
        GoalIconType.load();
        ProgressTrackerType.load();
        BingoSubType.load();

        BingoConditions.load();
        BingoEntitySubPredicates.load();
        BingoItemSubPredicates.load();
        BingoTriggers.load();
    }

    private static void registerDatapackRegistries() {
        BingoPlatform.platform.registerDatapackRegistries(registrar -> {
            registrar.unsynced(BingoRegistries.TAG, BingoTag.CODEC);
            registrar.unsynced(BingoRegistries.DIFFICULTY, BingoDifficulty.CODEC);
        });
    }

    private static void registerDataReloadListeners() {
        BingoPlatform.platform.registerDataReloadListeners(registrar -> {
            registrar.register(GoalManager.ID, GoalManager::new);
        });
    }

    private static void registerPayloadHandlers() {
        BingoNetworking.instance().onRegister(registrar -> {
            registrar.register(ConnectionProtocol.CONFIGURATION, null, ProtocolVersionPayload.TYPE, ProtocolVersionPayload.CODEC, (payload, context) -> {
                switch (context.flow()) {
                    case CLIENTBOUND -> payload.handleClientbound(context);
                    case SERVERBOUND -> payload.handleServerbound(context);
                }
            });

            registrar.register(PacketFlow.CLIENTBOUND, InitBoardPayload.TYPE, InitBoardPayload.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, RemoveBoardPayload.TYPE, RemoveBoardPayload.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, ResyncStatesPayload.TYPE, ResyncStatesPayload.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, SyncTeamPayload.TYPE, SyncTeamPayload.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, UpdateProgressPayload.TYPE, UpdateProgressPayload.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, UpdateStatePayload.TYPE, UpdateStatePayload.CODEC);
            registrar.register(PacketFlow.CLIENTBOUND, UpdateEndTimePayload.TYPE, UpdateEndTimePayload.CODEC);
        });
    }

    public static void updateCommandTree(PlayerList playerList) {
        playerList.getPlayers().forEach(playerList.getServer().getCommands()::sendCommands);
    }

    public static MutableComponent translatable(@Translatable String key, Object... args) {
        return BingoUtil.ensureHasFallback(Component.translatable(key, args));
    }

    public static MutableComponent translatableEscape(@Translatable(allowArbitraryArgs = true) String key, Object... args) {
        return BingoUtil.ensureHasFallback(Component.translatableEscape(key, args));
    }

    public static boolean isInstalledOnClient(ServerPlayer player) {
        return BingoNetworking.instance().canPlayerReceive(player, InitBoardPayload.TYPE);
    }
}
