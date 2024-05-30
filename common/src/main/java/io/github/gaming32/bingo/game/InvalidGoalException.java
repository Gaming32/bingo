package io.github.gaming32.bingo.game;

import net.minecraft.resources.ResourceLocation;

public class InvalidGoalException extends IllegalArgumentException {
    private final ResourceLocation goal;
    private final String reason;

    public InvalidGoalException(ResourceLocation goal, String reason) {
        super("Invalid goal " + goal + ": " + reason);
        this.goal = goal;
        this.reason = reason;
    }

    public InvalidGoalException(ResourceLocation goal, Throwable cause) {
        this(goal, cause.getMessage());
        initCause(cause);
    }

    public ResourceLocation getGoal() {
        return goal;
    }

    public String getReason() {
        return reason;
    }
}
