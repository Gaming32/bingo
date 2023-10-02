package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.ClientGoal;

public record ClientBoard(int size, BingoBoard.Teams[] states, ClientGoal[] goals) {
    public BingoBoard.Teams getState(int x, int y) {
        return states[getIndex(x, y)];
    }

    public ClientGoal getGoal(int x, int y) {
        return goals[getIndex(x, y)];
    }

    private int getIndex(int x, int y) {
        return y * size + x;
    }
}
