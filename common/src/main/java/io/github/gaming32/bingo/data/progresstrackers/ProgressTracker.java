package io.github.gaming32.bingo.data.progresstrackers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

public interface ProgressTracker {
    Codec<ProgressTracker> CODEC = BingoCodecs.registrarByName(ProgressTrackerType.REGISTRAR)
        .dispatch(ProgressTracker::type, ProgressTrackerType::codec);

    default DataResult<ProgressTracker> validate(BingoGoal goal) {
        return DataResult.success(this);
    }

    default void goalProgressChanged(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterion, int progress, int maxProgress) {
    }

    default void criterionChanged(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterion, boolean complete) {
    }

    ProgressTrackerType<?> type();

    @ApiStatus.NonExtendable
    default JsonObject serializeToJson() {
        return BingoUtil.toJsonObject(CODEC, this);
    }

    static ProgressTracker deserialize(JsonElement element) {
        return BingoUtil.fromJsonElement(CODEC, element);
    }
}
