package io.github.gaming32.bingo.game.persistence;

import com.mojang.serialization.Dynamic;
import io.github.gaming32.bingo.game.BingoGame;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.scores.Scoreboard;

public class PersistenceManager {
    public static final int CURRENT_DATA_VERSION = 3;

    public static CompoundTag serialize(HolderLookup.Provider registries, BingoGame game) {
        final var data = game.createPersistenceData();
        final var tag = BingoGame.PersistenceData.CODEC.encodeStart(
            registries.createSerializationContext(NbtOps.INSTANCE), data
        ).getOrThrow();
        if (!(tag instanceof CompoundTag compound)) {
            throw new IllegalStateException("Bingo game didn't serialize to CompoundTag");
        }
        NbtUtils.addDataVersion(compound, CURRENT_DATA_VERSION);
        return compound;
    }

    public static BingoGame deserialize(HolderLookup.Provider registries, CompoundTag tag, Scoreboard scoreboard) {
        final var version = NbtUtils.getDataVersion(tag, 1);

        final var newTag = PersistenceDataFixers.getDataFixer().update(
            PersistenceTypes.GAME, new Dynamic<>(NbtOps.INSTANCE, tag), version, CURRENT_DATA_VERSION
        ).getValue();

        final var data = BingoGame.PersistenceData.CODEC.parse(
            registries.createSerializationContext(NbtOps.INSTANCE), newTag
        ).getOrThrow();
        return data.createGame(scoreboard);
    }
}
