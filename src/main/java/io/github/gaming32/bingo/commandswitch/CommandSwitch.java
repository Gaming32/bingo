package io.github.gaming32.bingo.commandswitch;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
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
        private final String name;
        private final ArgumentType<A> type;
        private String argName;
        private ArgGetter<T> getter;
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

        public ArgumentSwitchBuilder<A, T> suggests(SuggestionProvider<CommandSourceStack> suggests) {
            this.suggests = suggests;
            return this;
        }

        public CommandSwitch<T> build(Function<CommandContext<CommandSourceStack>, T> defaultValue) {
            return buildInner(defaultValue);
        }

        public CommandSwitch<T> build(Supplier<T> defaultSupplier) {
            return buildInner(ctx -> defaultSupplier.get());
        }

        public CommandSwitch<T> build(T defaultValue) {
            return buildInner(ctx -> defaultValue);
        }

        public <C> CommandSwitch<C> buildRepeatable(Function<Collection<T>, C> collectionFunction) {
            return new RepeatableArgumentSwitch<>(
                buildInner(context -> null),
                (context, values) -> collectionFunction.apply(values)
            );
        }

        private ArgumentSwitch<A, T> buildInner(Function<CommandContext<CommandSourceStack>, T> defaultValue) {
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

        public ResourceArgumentSwitchBuilder<T> unknownExceptionType(DynamicCommandExceptionType type) {
            unknownExceptionType = type;
            return this;
        }

        public CommandSwitch<Holder.Reference<T>> build(ResourceKey<T> defaultValue) {
            final var registryKey = this.registry;
            final var exceptionType =
                Objects.requireNonNull(unknownExceptionType, "unknownExceptionType() must be called");
            return new ArgumentSwitch<>(
                name, argName,
                ResourceKeyArgument.key(registryKey),
                (context, arg) -> ResourceKeyArgument.resolveKey(context, arg, registry, exceptionType),
                context -> context
                    .getSource()
                    .registryAccess()
                    .lookupOrThrow(registryKey)
                    .getOrThrow(defaultValue),
                null
            );
        }

        public CommandSwitch<HolderSet<T>> buildRepeatable() {
            final var registryKey = this.registry;
            final var exceptionType =
                Objects.requireNonNull(unknownExceptionType, "unknownExceptionType() must be called");
            return new RepeatableArgumentSwitch<>(
                new ArgumentSwitch<>(
                    name, argName,
                    ResourceOrTagKeyArgument.resourceOrTagKey(registryKey),
                    (context, arg) ->
                        ResourceOrTagKeyArgument.getResourceOrTagKey(context, arg, registryKey, exceptionType),
                    context -> null, null
                ),
                (context, values) -> {
                    final var registry = context.getSource().registryAccess().lookupOrThrow(registryKey);
                    return switch (values.size()) {
                        case 0 -> HolderSet.empty();
                        case 1 -> BingoUtil.toHolderSet(registry, values.iterator().next());
                        default -> HolderSet.direct(
                            values.stream()
                                .map(result -> BingoUtil.toHolderSet(registry, result))
                                .flatMap(HolderSet::stream)
                                .distinct()
                                .toList()
                        );
                    };
                }
            );
        }
    }
}
