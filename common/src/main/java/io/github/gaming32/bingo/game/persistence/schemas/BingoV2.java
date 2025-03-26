package io.github.gaming32.bingo.game.persistence.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import io.github.gaming32.bingo.game.persistence.PersistenceTypes;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.mojang.datafixers.DSL.*;

public class BingoV2 extends NamespacedSchema {
    public BingoV2(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);

        schema.registerType(false, PersistenceTypes.CRITERION, () -> or(
            taggedChoiceLazy("trigger", namespacedString(), entityTypes),
            remainder()
        ));

        schema.registerType(true, PersistenceTypes.ICON, () -> or(
            taggedChoiceLazy("type", namespacedString(), blockEntityTypes),
            remainder()
        ));

        schema.registerType(false, PersistenceTypes.ACTIVE_GOAL, () -> fields(
            "icon", PersistenceTypes.ICON.in(schema),
            "criteria", compoundList(PersistenceTypes.CRITERION.in(schema))
        ));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        final var triggers = new HashMap<String, Supplier<TypeTemplate>>();
        registerCriterion(triggers, "bingo:has_some_items_from_tag", () -> fields(
            "tag", PersistenceTypes.ITEM_TAG.in(schema)
        ));
        registerCriterion(triggers, "bingo:has_some_food_items", () -> and(
            optional(field("tag", PersistenceTypes.ITEM_TAG_PREDICATE.in(schema))),
            remainder()
        ));
        return triggers;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        final var icons = new HashMap<String, Supplier<TypeTemplate>>();
        register(icons, "bingo:cycle_icon", () -> fields(
            "icons", list(PersistenceTypes.ICON.in(schema))
        ));
        register(icons, "bingo:indicator", () -> fields(
            "base", PersistenceTypes.ICON.in(schema),
            "indicator", PersistenceTypes.ICON.in(schema)
        ));
        register(icons, "bingo:item_tag_cycle", () -> fields(
            "tag", PersistenceTypes.ITEM_TAG.in(schema)
        ));
        return icons;
    }

    public static void registerCriterion(Map<String, Supplier<TypeTemplate>> map, String name, Supplier<TypeTemplate> template) {
        map.put(name, () -> fields("conditions", template.get()));
    }
}
