package io.github.gaming32.bingo.ext;

import io.github.gaming32.bingo.game.BingoGame;
import org.jetbrains.annotations.Nullable;

public interface MinecraftServerExt {
    @Nullable
    default BingoGame bingo$getGame() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default void bingo$setGame(@Nullable BingoGame game) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}
