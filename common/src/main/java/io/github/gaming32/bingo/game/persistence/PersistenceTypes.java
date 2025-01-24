package io.github.gaming32.bingo.game.persistence;

import com.mojang.datafixers.DSL;
import net.minecraft.util.datafix.fixes.References;

public class PersistenceTypes {
    // DFU crashes if we have no recursive types
    public static final DSL.TypeReference HACK_UNUSED_RECURSIVE = reference("hack_unused_recursive");

    public static final DSL.TypeReference ACTIVE_GOAL = reference("active_goal");
    public static final DSL.TypeReference BOARD = reference("board");
    public static final DSL.TypeReference GAME = reference("game");

    public static DSL.TypeReference reference(String name) {
        return References.reference("bingo:" + name);
    }
}
