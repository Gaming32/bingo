package io.github.gaming32.bingo.fabric.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BingoDataGenUtil {
    private static final Map<EntityType<?>, Class<? extends Entity>> ENTITY_TYPE_TO_CLASS = createEntityTypeToClassMap();
    private static final Map<Item, List<ResourceLocation>> RECIPES_BY_ITEM = createRecipesByItemMap();

    private BingoDataGenUtil() {
    }

    public static <T> HolderSet.Direct<T> loadVanillaTag(TagKey<T> tag, HolderLookup.Provider registries) {
        return HolderSet.direct(loadVanillaTagInternal(tag, registries));
    }

    private static <T> List<Holder<T>> loadVanillaTagInternal(TagKey<T> tag, HolderLookup.Provider registries) {
        final var registry = registries.lookupOrThrow(tag.registry());
        final IoSupplier<InputStream> resource = Minecraft.getInstance()
            .getVanillaPackResources()
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

        final TagFile file = TagFile.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json)
            .getOrThrow(JsonParseException::new);
        final var lookup = new TagEntry.Lookup<Holder<T>>() {
            @Nullable
            @Override
            public Holder<T> element(ResourceLocation elementLocation, boolean readOnly) {
                return registry.get(ResourceKey.create(tag.registry(), elementLocation)).orElse(null);
            }

            @Nullable
            @Override
            public Collection<Holder<T>> tag(ResourceLocation tagLocation) {
                return loadVanillaTagInternal(TagKey.create(tag.registry(), tagLocation), registries);
            }
        };
        return file.entries()
            .stream()
            .<Holder<T>>mapMulti((entry, out) -> entry.build(lookup, out))
            .distinct()
            .toList();
    }

    public static LootTable loadVanillaLootTable(ResourceKey<LootTable> lootTable, HolderLookup.Provider registries) {
        final IoSupplier<InputStream> resource = Minecraft.getInstance()
            .getVanillaPackResources()
            .getResource(
                PackType.SERVER_DATA, lootTable.location().withPath(path -> "loot_table/" + path + ".json")
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

        final DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        return LootTable.DIRECT_CODEC.parse(ops, json).getOrThrow(JsonParseException::new);
    }

    private static Map<EntityType<?>, Class<? extends Entity>> createEntityTypeToClassMap() {
        Map<EntityType<?>, Class<? extends Entity>> result = new HashMap<>();

        final int staticFinal = Modifier.STATIC | Modifier.FINAL;
        for (Field field : EntityType.class.getFields()) {
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

    private static Map<Item, List<ResourceLocation>> createRecipesByItemMap() {
        Map<Item, List<ResourceLocation>> recipesByItem = new HashMap<>();

        VanillaPackResources packResources = Minecraft.getInstance().getVanillaPackResources();
        Set<String> namespaces = packResources.getNamespaces(PackType.SERVER_DATA);
        for (String namespace : namespaces) {
            packResources.listResources(PackType.SERVER_DATA, namespace, "recipe", (recipeId, resource) -> {
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
                ResourceLocation resultId = ResourceLocation.tryParse(id);
                Item item = BuiltInRegistries.ITEM.getValue(resultId);
                if (item != Items.AIR) {
                    recipesByItem.computeIfAbsent(item, k -> new ArrayList<>()).add(convertRecipeId(recipeId));
                }
            });
        }

        return recipesByItem;
    }

    private static ResourceLocation convertRecipeId(ResourceLocation fullPath) {
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

    public static List<ResourceLocation> getRecipesForItem(Item item) {
        return RECIPES_BY_ITEM.getOrDefault(item, List.of());
    }
}
