package io.github.gaming32.bingo.game;

import net.minecraft.resources.Identifier;

public class InvalidGoalException extends IllegalStateException {
    private final Identifier goal;
    private final String reason;

    public InvalidGoalException(Identifier goal, String reason) {
        super("Invalid goal " + goal + ": " + reason);
        this.goal = goal;
        this.reason = reason;
    }

    public InvalidGoalException(Identifier goal, Throwable cause) {
        this(goal, cause.getMessage());
        initCause(cause);
    }

    public Identifier getGoal() {
        return goal;
    }

    public String getReason() {
        return reason;
    }
}
