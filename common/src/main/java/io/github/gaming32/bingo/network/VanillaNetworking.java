package io.github.gaming32.bingo.network;

import com.google.common.collect.ImmutableMap;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class VanillaNetworking {
    public static final AdvancementHolder ROOT_ADVANCEMENT = new AdvancementHolder(
        ResourceLocations.bingo("generated/root"),
        new Advancement(
            Optional.empty(),
            Optional.of(new DisplayInfo(
                new ItemStack(Items.PLAYER_HEAD),
                Bingo.translatable("bingo.board.title"),
                CommonComponents.EMPTY,
                Optional.of(new ClientAsset(ResourceLocations.minecraft("gui/advancements/backgrounds/stone"))),
                AdvancementType.TASK,
                false,
                false,
                true
            )),
            AdvancementRewards.EMPTY,
            ImmutableMap.of(),
            AdvancementRequirements.EMPTY,
            false
        )
    );

    public static final String CRITERION = "criterion";
    public static final AdvancementRequirements REQUIREMENTS = AdvancementRequirements.allOf(List.of(CRITERION));

    public static List<AdvancementHolder> generateAdvancements(
        RegistryAccess registries, int size, ActiveGoal[] goals
    ) {
        final List<AdvancementHolder> result = new ArrayList<>(1 + goals.length);
        result.add(ROOT_ADVANCEMENT);
        for (int i = 0; i < goals.length; i++) {
            result.add(generateAdvancement(registries, i, goals[i], i % size, i / size));
        }
        return result;
    }

    public static AdvancementHolder generateAdvancement(
        RegistryAccess registries, int index, ActiveGoal goal, int x, int y
    ) {
        final DisplayInfo displayInfo = new DisplayInfo(
            goal.icon().getFallback(registries),
            goal.name(),
            goal.tooltip().orElse(CommonComponents.EMPTY),
            Optional.empty(),
            AdvancementType.TASK,
            false,
            false,
            false
        );
        displayInfo.setLocation(x + 0.5f, y);
        return new AdvancementHolder(
            BingoBoard.generateVanillaId(index),
            new Advancement(
                Optional.of(ROOT_ADVANCEMENT.id()),
                Optional.of(displayInfo),
                AdvancementRewards.EMPTY,
                ImmutableMap.of(),
                REQUIREMENTS,
                false
            )
        );
    }

    public static Map<ResourceLocation, AdvancementProgress> generateProgressMap(
        BingoBoard.Teams[] board, BingoBoard.Teams playerTeam
    ) {
        final Map<ResourceLocation, AdvancementProgress> result = HashMap.newHashMap(board.length);
        for (int i = 0; i < board.length; i++) {
            result.put(BingoBoard.generateVanillaId(i), generateProgress(board[i].and(playerTeam)));
        }
        return result;
    }

    public static AdvancementProgress generateProgress(boolean complete) {
        final AdvancementProgress result = new AdvancementProgress();
        result.update(REQUIREMENTS);
        if (complete) {
            //noinspection DataFlowIssue
            result.getCriterion(CRITERION).grant();
        }
        return result;
    }

    public static Set<ResourceLocation> generateAdvancementIds(int count) {
        final Set<ResourceLocation> result = HashSet.newHashSet(1 + count);
        result.add(ROOT_ADVANCEMENT.id());
        IntStream.range(0, count)
            .mapToObj(BingoBoard::generateVanillaId)
            .forEach(result::add);
        return result;
    }
}
