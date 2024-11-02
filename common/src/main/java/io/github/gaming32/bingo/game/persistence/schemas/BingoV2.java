package io.github.gaming32.bingo.game.persistence.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import io.github.gaming32.bingo.game.persistence.PersistenceTypes;

import java.util.Map;
import java.util.function.Supplier;

public class BingoV2 extends Schema {
    public BingoV2(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(false, PersistenceTypes.ACTIVE_GOAL, DSL::remainder);
    }
}
