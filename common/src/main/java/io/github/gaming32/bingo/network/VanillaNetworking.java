package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import net.minecraft.advancements.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.stream.IntStream;

public class VanillaNetworking {
    public static final ResourceLocation ROOT_ID = new ResourceLocation("bingo:generated/root");
    public static final Advancement ROOT_ADVANCEMENT = new Advancement(
        ROOT_ID,
        null,
        new DisplayInfo(
            new ItemStack(Items.PLAYER_HEAD),
            Bingo.translatable("bingo.board.title"),
            CommonComponents.EMPTY,
            new ResourceLocation("minecraft:textures/gui/advancements/backgrounds/stone.png"),
            FrameType.TASK,
            false,
            false,
            true
        ),
        AdvancementRewards.EMPTY,
        Map.of(),
        new String[0][0],
        false
    );

    public static final String CRITERION = "criterion";
    public static final Map<String, Criterion> CRITERIA = Map.of(CRITERION, new Criterion());
    public static final String[][] REQUIREMENTS = {{CRITERION}};

    public static List<Advancement> generateAdvancements(ActiveGoal[] goals) {
        final List<Advancement> result = new ArrayList<>(1 + goals.length);
        result.add(ROOT_ADVANCEMENT);
        for (int i = 0; i < goals.length; i++) {
            result.add(generateAdvancement(i, goals[i], i % BingoBoard.SIZE, i / BingoBoard.SIZE));
        }
        return result;
    }

    public static Advancement generateAdvancement(int index, ActiveGoal goal, int x, int y) {
        final DisplayInfo displayInfo = new DisplayInfo(
            goal.getIcon(),
            goal.getName(),
            Objects.requireNonNullElse(goal.getTooltip(), CommonComponents.EMPTY),
            null,
            FrameType.TASK,
            false,
            false,
            false
        );
        displayInfo.setLocation(x + 0.5f, y);
        return new Advancement(
            generateAdvancementId(index),
            ROOT_ADVANCEMENT,
            displayInfo,
            AdvancementRewards.EMPTY,
            CRITERIA,
            REQUIREMENTS,
            false
        );
    }

    public static Map<ResourceLocation, AdvancementProgress> generateProgressMap(
        BingoBoard.Teams[] board, BingoBoard.Teams playerTeam
    ) {
        final Map<ResourceLocation, AdvancementProgress> result = new HashMap<>(board.length);
        for (int i = 0; i < board.length; i++) {
            result.put(generateAdvancementId(i), generateProgress(board[i].and(playerTeam)));
        }
        return result;
    }

    public static AdvancementProgress generateProgress(boolean complete) {
        final AdvancementProgress result = new AdvancementProgress();
        result.update(CRITERIA, REQUIREMENTS);
        if (complete) {
            //noinspection DataFlowIssue
            result.getCriterion(CRITERION).grant();
        }
        return result;
    }

    public static Set<ResourceLocation> generateAdvancementIds(int count) {
        final Set<ResourceLocation> result = new HashSet<>(1 + count);
        result.add(ROOT_ID);
        IntStream.range(0, count)
            .mapToObj(VanillaNetworking::generateAdvancementId)
            .forEach(result::add);
        return result;
    }

    public static ResourceLocation generateAdvancementId(int index) {
        return new ResourceLocation("bingo:generated/goal/" + index);
    }
}
