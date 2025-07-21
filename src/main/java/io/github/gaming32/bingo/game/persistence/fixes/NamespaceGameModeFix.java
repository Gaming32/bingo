package io.github.gaming32.bingo.game.persistence.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import io.github.gaming32.bingo.game.persistence.PersistenceTypes;

import static com.mojang.datafixers.DSL.remainderFinder;

public class NamespaceGameModeFix extends DataFix {
    public NamespaceGameModeFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        final var gameType = getInputSchema().getType(PersistenceTypes.GAME);
        return fixTypeEverywhereTyped("NamespaceGameModeFix", gameType, game -> game.update(
            remainderFinder(), data -> data.update("game_mode", mode ->
                mode.createString("bingo:" + mode.asString(""))
            )
        ));
    }
}
