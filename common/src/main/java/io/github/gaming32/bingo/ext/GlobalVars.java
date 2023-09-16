package io.github.gaming32.bingo.ext;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.LinkedList;

public final class GlobalVars {
    private static final ThreadLocal<Deque<Player>> currentPlayer = ThreadLocal.withInitial(LinkedList::new);

    private GlobalVars() {
    }

    public static void pushCurrentPlayer(@Nullable Player currentPlayer) {
        GlobalVars.currentPlayer.get().addLast(currentPlayer);
    }

    public static void popCurrentPlayer() {
        GlobalVars.currentPlayer.get().removeLast();
    }

    @Nullable
    public static Player getCurrentPlayer() {
        return GlobalVars.currentPlayer.get().peekLast();
    }
}
