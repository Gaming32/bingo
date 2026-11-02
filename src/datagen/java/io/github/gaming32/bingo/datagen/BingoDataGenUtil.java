package io.github.gaming32.bingo.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.EmptyTagLookupWrapper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class BingoDataGenUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<EntityType<?>, Class<? extends Entity>> ENTITY_TYPE_TO_CLASS = createEntityTypeToClassMap();
    private static final Map<Item, List<Identifier>> RECIPES_BY_ITEM = createRecipesByItemMap();

    private BingoDataGenUtil() {
    }

    public static <T> HolderSet.Direct<T> loadVanillaTag(TagKey<T> tag, DatagenRegistryLookup context) {
        return HolderSet.direct(loadVanillaTagInternal(tag, context));
    }

    private static <T> List<Holder<T>> loadVanillaTagInternal(TagKey<T> tag, DatagenRegistryLookup context) {
        final var registry = context.lookup(tag.registry());
        final IoSupplier<InputStream> resource = Minecraft.getInstance()
            .getVanillaPackResources()
            .fullResources()
            .getResource(
                PackType.SERVER_DATA, tag.location().withPath(
                    Registries.tagsDirPath(tag.registry()) + '/' + tag.location().getPath() + ".json"
                )
            );
        if (resource == null) {
            throw new IllegalArgumentException("Unknown tag " + tag);
        }
        final JsonElement json;
        try (Reader input = new InputStreamReader(resource.get())) {
            json = JsonParser.parseReader(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        RegistryOps<JsonElement> ops = createRegistryOps(JsonOps.INSTANCE, context);
        final TagFile file = TagFile.CODEC.parse(ops, json).getOrThrow(JsonParseException::new);
        final var lookup = new TagEntry.Lookup<Holder<T>>() {
            @Nullable
            @Override
            public Holder<T> element(Identifier elementLocation, boolean readOnly) {
                return registry.get(ResourceKey.create(tag.registry(), elementLocation)).orElse(null);
            }

            @Override
            public Collection<Holder<T>> tag(Identifier tagLocation) {
                return loadVanillaTagInternal(TagKey.create(tag.registry(), tagLocation), context);
            }
        };
        return file.entries()
            .stream()
            .<Holder<T>>mapMulti((entry, out) -> entry.build(lookup, out))
            .distinct()
            .toList();
    }

    public static LootTable loadVanillaLootTable(ResourceKey<LootTable> lootTable, DatagenRegistryLookup context) {
        final IoSupplier<InputStream> resource = Minecraft.getInstance()
            .getVanillaPackResources()
            .fullResources()
            .getResource(
                PackType.SERVER_DATA, lootTable.identifier().withPath(path -> "loot_table/" + path + ".json")
            );
        if (resource == null) {
            throw new IllegalArgumentException("Unknown loot table " + lootTable);
        }

        final JsonElement json;
        try (Reader input = new InputStreamReader(resource.get())) {
            json = JsonParser.parseReader(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        final DynamicOps<JsonElement> ops = createRegistryOps(JsonOps.INSTANCE, context);
        return LootTable.DIRECT_CODEC.parse(ops, json).getOrThrow(JsonParseException::new);
    }

    private static Map<EntityType<?>, Class<? extends Entity>> createEntityTypeToClassMap() {
        Map<EntityType<?>, Class<? extends Entity>> result = new HashMap<>();

        final int staticFinal = Modifier.STATIC | Modifier.FINAL;
        for (Field field : EntityTypes.class.getFields()) {
            if ((field.getModifiers() & staticFinal) != staticFinal) {
                continue;
            }
            if (!EntityType.class.isAssignableFrom(field.getType())) {
                continue;
            }
            if (!(field.getGenericType() instanceof ParameterizedType type)) {
                continue;
            }
            Type[] typeArgs = type.getActualTypeArguments();
            if (typeArgs.length != 1) {
                continue;
            }
            if (!(typeArgs[0] instanceof Class<?> entityTypeClass)) {
                continue;
            }

            EntityType<?> entityType;
            try {
                entityType = (EntityType<?>) field.get(null);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }

            result.put(entityType, entityTypeClass.asSubclass(Entity.class));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Entity> Class<T> getEntityTypeClass(EntityType<T> entityType) {
        return (Class<T>) ENTITY_TYPE_TO_CLASS.get(entityType);
    }

    private static Map<Item, List<Identifier>> createRecipesByItemMap() {
        Map<Item, List<Identifier>> recipesByItem = new HashMap<>();

        VanillaPackResources packResources = Minecraft.getInstance().getVanillaPackResources();
        Set<String> namespaces = packResources.fullResources().getNamespaces(PackType.SERVER_DATA);
        for (String namespace : namespaces) {
            packResources.fullResources().listResources(PackType.SERVER_DATA, namespace, "recipe", (recipeId, resource) -> {
                final JsonElement json;
                try (Reader input = new InputStreamReader(resource.get())) {
                    json = JsonParser.parseReader(input);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

                if (!json.isJsonObject()) {
                    return;
                }
                JsonObject result = GsonHelper.getAsJsonObject(json.getAsJsonObject(), "result", new JsonObject());
                String id = GsonHelper.getAsString(result, "id", null);
                if (id == null) {
                    return;
                }
                Identifier resultId = Identifier.tryParse(id);
                Item item = BuiltInRegistries.ITEM.getValue(resultId);
                if (item != Items.AIR) {
                    recipesByItem.computeIfAbsent(item, k -> new ArrayList<>()).add(convertRecipeId(recipeId));
                }
            });
        }

        recipesByItem.values().forEach(Collections::sort);
        return recipesByItem;
    }

    private static Identifier convertRecipeId(Identifier fullPath) {
        return fullPath.withPath(path -> {
            if (path.startsWith("recipe/")) {
                path = path.substring("recipe/".length());
            }
            if (path.endsWith(".json")) {
                path = path.substring(0, path.length() - ".json".length());
            }
            return path;
        });
    }

    public static List<Identifier> getRecipesForItem(Item item) {
        return RECIPES_BY_ITEM.getOrDefault(item, List.of());
    }

    @SuppressWarnings("unchecked")
    public static <T> List<ResourceKey<T>> getResourceKeysFromClass(Class<?> containingClass, ResourceKey<? extends Registry<? extends T>> registryKey) {
        List<ResourceKey<T>> result = new ArrayList<>();

        for (Field field : containingClass.getFields()) {
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            if (field.getType() != ResourceKey.class) {
                continue;
            }
            ResourceKey<?> resourceKey;
            try {
                resourceKey = (ResourceKey<?>) field.get(null);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Error getting field '{}'", field.getName(), e);
                continue;
            }
            if (!resourceKey.isFor(registryKey)) {
                continue;
            }
            result.add((ResourceKey<T>) resourceKey);
        }

        return result;
    }

    public static <T> Holder.Reference<T> fakeElement(HolderGetter<T> registry, ResourceKey<T> recipeKey) {
        return Holder.Reference.createStandAlone(
            registry instanceof EmptyTagLookupWrapper<T>(var parent) ? parent : registry,
            recipeKey
        );
    }

    public static <T> HolderGetter<T> fakeLookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
        record FakeLookup<T>(ResourceKey<? extends Registry<? extends T>> registryKey) implements HolderGetter<T> {
            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
                throw new UnsupportedOperationException("Cannot get element out of fake lookup");
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
                throw new UnsupportedOperationException("Cannot get tag out of fake lookup");
            }

            @Override
            public boolean canSerialize(HolderOwner<T> owner) {
                return equals(owner);
            }
        }
        return new FakeLookup<>(registryKey);
    }

    public static Holder<LootItemCondition> allOf(LootItemCondition.Builder... conditions) {
        return Holder.direct(AllOfCondition.allOf(conditions).build());
    }

    public static Holder<LootItemCondition> allOf(LootItemCondition... conditions) {
        return Holder.direct(AllOfCondition.allOf(HolderSet.direct(Holder::direct, conditions)));
    }

    @SafeVarargs
    public static Holder<LootItemCondition> allOf(Holder<LootItemCondition>... conditions) {
        return Holder.direct(AllOfCondition.allOf(HolderSet.direct(conditions)));
    }

    public static <T> RegistryOps<T> createRegistryOps(DynamicOps<T> parent, DatagenRegistryLookup context) {
        return RegistryOps.create(parent, new RegistryOps.RegistryInfoLookup() {
            @Override
            public <R> Optional<HolderGetter<R>> lookup(ResourceKey<? extends Registry<? extends R>> registryKey) {
                try {
                    return Optional.of(context.lookup(registryKey));
                } catch (Exception _) {
                    return Optional.of(fakeLookup(registryKey));
                }
            }
        });
    }

    @FunctionalInterface
    public interface DatagenRegistryLookup {
        <T> HolderGetter<T> lookup(ResourceKey<? extends Registry<? extends T>> registryKey);
    }
}
