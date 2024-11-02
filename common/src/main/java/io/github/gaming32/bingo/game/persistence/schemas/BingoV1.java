package io.github.gaming32.bingo.game.persistence.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import io.github.gaming32.bingo.game.persistence.PersistenceTypes;

import java.util.Map;
import java.util.function.Supplier;

import static com.mojang.datafixers.DSL.fields;
import static com.mojang.datafixers.DSL.list;

public class BingoV1 extends Schema {
    public BingoV1(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(
        Schema schema,
        Map<String, Supplier<TypeTemplate>> entityTypes,
        Map<String, Supplier<TypeTemplate>> blockEntityTypes
    ) {
        schema.registerType(true, PersistenceTypes.HACK_UNUSED_RECURSIVE, DSL::remainder);

        schema.registerType(false, PersistenceTypes.ACTIVE_GOAL, DSL::remainder);
        schema.registerType(false, PersistenceTypes.BOARD, () -> fields(
            "goals", list(PersistenceTypes.ACTIVE_GOAL.in(schema))
        ));
        schema.registerType(false, PersistenceTypes.GAME, () -> fields(
            "board", PersistenceTypes.BOARD.in(schema)
        ));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        return Map.of();
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        return Map.of();
    }
}
