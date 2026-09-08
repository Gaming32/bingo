package io.github.gaming32.bingo.mixin;

import io.github.gaming32.bingo.ext.MinecraftServerExt;
import io.github.gaming32.bingo.game.BingoGame;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements MinecraftServerExt {
    @Unique
    @Nullable
    private BingoGame bingo$game;

    @Override
    @Nullable
    public BingoGame bingo$getGame() {
        return bingo$game;
    }

    @Override
    public void bingo$setGame(@Nullable BingoGame game) {
        bingo$game = game;
    }
}
