package io.github.gaming32.bingo.ext;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.LinkedList;

public final class GlobalVars {
    public static final ThreadLocalStack<ItemStack> CURRENT_ITEM = new ThreadLocalStack<>();
    public static final ThreadLocalStack<Player> CURRENT_PLAYER = new ThreadLocalStack<>();
    public static final ThreadLocalStack<Projectile> CURRENT_PROJECTILE = new ThreadLocalStack<>();
    public static final ThreadLocalStack<BlockPos> CURRENT_BLOCK_POS = new ThreadLocalStack<>();
    public static final ThreadLocalStack<Integer> CURRENT_REDSTONE_OUTPUT = new ThreadLocalStack<>();

    private GlobalVars() {
    }

    public static final class ThreadLocalStack<E> {
        private final ThreadLocal<Deque<E>> stack = ThreadLocal.withInitial(LinkedList::new);
        private final PushContext pushContext = new PushContext();

        public PushContext push(E value) {
            stack.get().addLast(value);
            return pushContext;
        }

        @Nullable
        public E get() {
            return stack.get().peekLast();
        }

        public E getOrElse(E fallback) {
            final Deque<E> stack = this.stack.get();
            if (stack.isEmpty()) {
                return fallback;
            }
            return stack.getLast();
        }

        public class PushContext implements AutoCloseable {
            private PushContext() {
            }

            @Override
            public void close() {
                stack.get().removeLast();
            }
        }
    }
}
