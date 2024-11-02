package io.github.gaming32.bingo.game.persistence;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import io.github.gaming32.bingo.game.persistence.fixes.FlattenGoalFix;
import io.github.gaming32.bingo.game.persistence.schemas.BingoV1;
import io.github.gaming32.bingo.game.persistence.schemas.BingoV2;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class PersistenceDataFixers {
    private static final DataFixerBuilder.Result DATA_FIXER = createFixerUpper();

    public static DataFixer getDataFixer() {
        return DATA_FIXER.fixer();
    }

    private static DataFixerBuilder.Result createFixerUpper() {
        final var builder = new DataFixerBuilder(PersistenceManager.CURRENT_DATA_VERSION);
        addFixers(builder);
        return builder.build();
    }

    public static CompletableFuture<?> optimize(Set<DSL.TypeReference> types) {
        if (types.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        final var executor = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().name("Bingo Datafixer Bootstrap").daemon().priority(1).factory()
        );
        return DATA_FIXER.optimize(types, executor);
    }

    private static void addFixers(DataFixerBuilder builder) {
        builder.addSchema(1, BingoV1::new);

        final var v2 = builder.addSchema(2, BingoV2::new);
        builder.addFixer(new FlattenGoalFix(v2));
    }
}
