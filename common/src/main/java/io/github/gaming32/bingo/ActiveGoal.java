package io.github.gaming32.bingo;

import io.github.gaming32.bingo.data.BingoGoal;
import net.minecraft.advancements.Criterion;
import net.minecraft.network.chat.Component;

import java.util.Map;

public record ActiveGoal(BingoGoal goal, Component name, Map<String, Criterion> criteria) {
}
