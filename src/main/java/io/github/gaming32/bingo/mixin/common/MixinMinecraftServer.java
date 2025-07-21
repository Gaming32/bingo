package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.MinecraftServerExt;
import io.github.gaming32.bingo.game.BingoGame;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements MinecraftServerExt {
    @Unique
    private BingoGame bingo$game;

    @Override
    public BingoGame bingo$getGame() {
        return bingo$game;
    }

    @Override
    public void bingo$setGame(BingoGame game) {
        bingo$game = game;
    }
}
