package io.github.gaming32.bingo.commandswitch;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface CommandSwitch<T> permits ArgumentSwitch, ConstantSwitch, RepeatableArgumentSwitch {
    String name();

    void addTo(CommandNode<CommandSourceStack> node);

    T get(CommandContext<CommandSourceStack> context);

    static CommandSwitch<Boolean> storeTrue(String name) {
        return storeConstant(name, true, false);
    }

    static CommandSwitch<Boolean> storeFalse(String name) {
        return storeConstant(name, false, true);
    }

    static <T> CommandSwitch<@Nullable T> storeConstant(String name, T value) {
        return storeConstant(name, value, null);
    }

    static <T> CommandSwitch<T> storeConstant(String name, T value, T fallback) {
        return new ConstantSwitch<>(name, value, fallback);
    }

    static <T> ArgumentSwitchBuilder<T, T> argument(String name, ArgumentType<T> type) {
        return new ArgumentSwitchBuilder<>(name, type);
    }

    static <A, T> ArgumentSwitchBuilder<A, T> specialArgument(String name, ArgumentType<A> type) {
        return new ArgumentSwitchBuilder<>(name, type);
    }

    static ArgumentSwitchBuilder<Integer, Integer> argument(String name, IntegerArgumentType type) {
        return argument(name, (ArgumentType<Integer>)type).getter(IntegerArgumentType::getInteger);
    }

    static ArgumentSwitchBuilder<Long, Long> argument(String name, LongArgumentType type) {
        return argument(name, (ArgumentType<Long>)type).getter(LongArgumentType::getLong);
    }

    static ArgumentSwitchBuilder<String, String> argument(String name, StringArgumentType type) {
        return argument(name, (ArgumentType<String>)type).getter(StringArgumentType::getString);
    }

    static <T> ResourceArgumentSwitchBuilder<T> resource(String name, ResourceKey<Registry<T>> registry) {
        return new ResourceArgumentSwitchBuilder<>(name, registry);
    }

    final class ArgumentSwitchBuilder<A, T> {
        private static final Function<CommandContext<CommandSourceStack>, ?> ALWAYS_NULL = context -> null;

        private final String name;
        private final ArgumentType<A> type;
        private String argName;
        private ArgGetter<T> getter;
        @SuppressWarnings("unchecked")
        private Function<CommandContext<CommandSourceStack>, T> defaultValue =
            (Function<CommandContext<CommandSourceStack>, T>)ALWAYS_NULL;
        private SuggestionProvider<CommandSourceStack> suggests = null;

        private ArgumentSwitchBuilder(String name, ArgumentType<A> type) {
            this.name = name;
            this.type = type;
            argName = inferArgName(name);
        }

        private static String inferArgName(String name) {
            return StringUtils.defaultIfEmpty(StringUtils.substringAfterLast(name, '-'), name);
        }

        public ArgumentSwitchBuilder<A, T> argName(String argName) {
            this.argName = argName;
            return this;
        }

        public ArgumentSwitchBuilder<A, T> getter(ArgGetter<T> getter) {
            this.getter = getter;
            return this;
        }

        public ArgumentSwitchBuilder<A, T> defaultValue(Function<CommandContext<CommandSourceStack>, T> function) {
            this.defaultValue = function;
            return this;
        }

        public ArgumentSwitchBuilder<A, T> defaultValue(Supplier<T> supplier) {
            return defaultValue(context -> supplier.get());
        }

        public ArgumentSwitchBuilder<A, T> defaultValue(T value) {
            return defaultValue(context -> value);
        }

        public ArgumentSwitchBuilder<A, T> suggests(SuggestionProvider<CommandSourceStack> suggests) {
            this.suggests = suggests;
            return this;
        }

        public CommandSwitch<T> build() {
            return buildInner();
        }

        public <C> CommandSwitch<C> buildRepeatable(Function<Collection<T>, C> collectionFunction) {
            if (defaultValue != ALWAYS_NULL) {
                throw new IllegalArgumentException("Cannot specify default value with buildRepeatable()");
            }
            return new RepeatableArgumentSwitch<>(buildInner(), collectionFunction);
        }

        private ArgumentSwitch<A, T> buildInner() {
            return new ArgumentSwitch<>(
                name, argName, type,
                Objects.requireNonNull(getter, "Must specify a getter() for a argument()"),
                defaultValue, suggests
            );
        }
    }

    final class ResourceArgumentSwitchBuilder<T> {
        private final String name;
        private final ResourceKey<Registry<T>> registry;
        private String argName;
        private ResourceKey<T> defaultValue;
        private DynamicCommandExceptionType unknownExceptionType;

        private ResourceArgumentSwitchBuilder(String name, ResourceKey<Registry<T>> registry) {
            this.name = name;
            this.registry = registry;
            argName = ArgumentSwitchBuilder.inferArgName(name);
        }

        public ResourceArgumentSwitchBuilder<T> argName(String argName) {
            this.argName = argName;
            return this;
        }

        public ResourceArgumentSwitchBuilder<T> defaultValue(ResourceKey<T> defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public ResourceArgumentSwitchBuilder<T> unknownExceptionType(DynamicCommandExceptionType type) {
            unknownExceptionType = type;
            return this;
        }

        public CommandSwitch<Holder.Reference<T>> build() {
            return buildInner();
        }

        public CommandSwitch<HolderSet<T>> buildRepeatable() {
            if (defaultValue != null) {
                throw new IllegalArgumentException("Cannot specify default value with buildRepeatable()");
            }
            return new RepeatableArgumentSwitch<>(
                buildInner(),
                values -> HolderSet.direct(values.stream().distinct().toList())
            );
        }

        private ArgumentSwitch<ResourceKey<T>, Holder.Reference<T>> buildInner() {
            final var registryKey = this.registry;
            final var defaultKey = this.defaultValue;
            return new ArgumentSwitch<>(
                name, argName,
                ResourceKeyArgument.key(registryKey),
                ArgGetter.forResource(
                    registryKey,
                    Objects.requireNonNull(unknownExceptionType, "unknownExceptionType() must be called")
                ),
                context -> {
                    final var registry = context.getSource().registryAccess().lookupOrThrow(registryKey);
                    final ResourceKey<T> key;
                    if (defaultKey != null) {
                        key = defaultKey;
                    } else if (registry instanceof DefaultedRegistry<T> defaulted) {
                        key = ResourceKey.create(registryKey, defaulted.getDefaultKey());
                    } else {
                        throw new IllegalArgumentException("defaultValue() must be specified for non-defaulted registry");
                    }
                    return registry.getOrThrow(key);
                },
                null
            );
        }
    }
}
