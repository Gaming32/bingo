package io.github.gaming32.bingo.multiloader;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Event<T> {
    public static final Event<CommandRegistrar> REGISTER_COMMANDS = new Event<>();
    public static final Event<Consumer<ServerPlayer>> PLAYER_JOIN = new Event<>();
    public static final Event<Consumer<ServerPlayer>> PLAYER_QUIT = new Event<>();
    public static final Event<Consumer<MinecraftServer>> SERVER_STARTED = new Event<>();
    public static final Event<Consumer<MinecraftServer>> SERVER_STOPPING = new Event<>();
    public static final Event<Consumer<MinecraftServer>> SERVER_STOPPED = new Event<>();
    public static final Event<BiConsumer<Player, InteractionHand>> RIGHT_CLICK_ITEM = new Event<>();
    public static final Event<BiConsumer<Level, Explosion>> EXPLOSION_START = new Event<>();
    public static final Event<Consumer<MinecraftServer>> SERVER_TICK_END = new Event<>();

    private Consumer<T> registrar;

    public void setRegistrar(Consumer<T> registrar) {
        this.registrar = registrar;
    }

    public void register(T handler) {
        registrar.accept(handler);
    }

    @FunctionalInterface
    public interface CommandRegistrar {
        void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registry,
            Commands.CommandSelection selection
        );
    }
}
