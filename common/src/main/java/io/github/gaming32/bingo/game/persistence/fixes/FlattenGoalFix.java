package io.github.gaming32.bingo.game.persistence.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import io.github.gaming32.bingo.game.persistence.PersistenceTypes;

public class FlattenGoalFix extends DataFix {
    public FlattenGoalFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        final var input = getInputSchema().getType(PersistenceTypes.ACTIVE_GOAL);
        final var output = getOutputSchema().getType(PersistenceTypes.ACTIVE_GOAL);
        return writeFixAndRead("FlattenGoalFix", input, output, this::fix);
    }

    private Dynamic<?> fix(Dynamic<?> goal) {
        final var holder = goal.get("goal").orElseEmptyMap();
        final var realGoal = holder.get("goal").orElseEmptyMap();
        goal = goal.remove("goal");
        goal = copyFieldFrom(goal, holder, "id");
        goal = copyFieldFrom(goal, realGoal, "tooltip_icon");
        goal = copyFieldFrom(goal, realGoal, "difficulty");
        goal = copyFieldFrom(goal, realGoal, "requirements");
        goal = copyFieldFrom(goal, realGoal, "special_type");
        goal = copyFieldFrom(goal, realGoal, "progress");
        return goal;
    }

    private static Dynamic<?> copyFieldFrom(Dynamic<?> result, Dynamic<?> from, String field) {
        return result.setFieldIfPresent(field, from.get(field).result());
    }
}
