package io.github.gaming32.bingo.game.mode;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.goal.GoalHolder;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryBuilder;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import io.github.gaming32.bingo.util.BingoStreamCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface BingoGameMode {
    DeferredRegister<BingoGameMode> REGISTER = new RegistryBuilder<>(BingoRegistries.GAME_MODE)
        .defaultId("standard")
        .build();

    RegistryValue<BingoGameMode> STANDARD = REGISTER.register("standard", StandardGameMode::new);
    RegistryValue<BingoGameMode> LOCKOUT = REGISTER.register("lockout", LockoutGameMode::new);
    RegistryValue<BingoGameMode> BLACKOUT = REGISTER.register("blackout", BlackoutGameMode::new);

    Codec<BingoGameMode> PERSISTENCE_CODEC = REGISTER.registry().byNameCodec();

    @Nullable
    default CommandSyntaxException checkAllowedConfig(GameConfig config) {
        return null;
    }

    BingoBoard.@NotNull Teams getWinners(BingoBoard board, int teamCount, BingoBoard.Teams nerfedTeams, boolean tryHarder);

    boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team, boolean isNever);

    default boolean isGoalAllowed(GoalHolder goal) {
        return true;
    }

    default boolean announceGoal(BingoGame game, BingoBoard.Teams team, int goalIndex) {
        return game.getNerfedTeams().and(team) || !game.getBoard().getShape().isNerfCell(game.getBoard().getSize(), goalIndex);
    }

    default RenderMode getRenderMode() {
        return RenderMode.FANCY;
    }

    default boolean isLockout() {
        return false;
    }

    default boolean canFinishedTeamsGetMoreGoals() {
        return true;
    }

    static boolean hasGoal(BingoBoard board, int goalIndex, BingoBoard.Teams team, boolean isNerfed) {
        if (!isNerfed && board.getShape().isNerfCell(board.getSize(), goalIndex)) {
            return true;
        }

        return board.getStates()[goalIndex].and(team);
    }

    static void load() {
    }

    record GameConfig(BingoGameMode gameMode, int size, Collection<PlayerTeam> teams) {
    }

    enum RenderMode {
        FANCY, ALL_TEAMS;

        public static final StreamCodec<FriendlyByteBuf, RenderMode> STREAM_CODEC = BingoStreamCodecs.enum_(RenderMode.class);
    }
}
