package io.github.gaming32.bingo.ext;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Deque;
import java.util.LinkedList;

public final class GlobalVars {
    public static final ThreadLocalStack<ItemStack> CURRENT_ITEM = new ThreadLocalStack<>();
    public static final ThreadLocalStack<Player> CURRENT_PLAYER = new ThreadLocalStack<>();

    private GlobalVars() {
    }

    public static final class ThreadLocalStack<E> {
        private final ThreadLocal<Deque<E>> stack = ThreadLocal.withInitial(LinkedList::new);
        private final PushContext pushContext = new PushContext();

        public void push(E value) {
            stack.get().addLast(value);
        }

        public PushContext pushed(E value) {
            push(value);
            return pushContext;
        }

        public E pop() {
            return stack.get().removeLast();
        }

        public E peek() {
            return stack.get().peekLast();
        }

        public class PushContext implements AutoCloseable {
            private PushContext() {
            }

            @Override
            public void close() {
                pop();
            }
        }
    }
}
