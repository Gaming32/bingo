package io.github.gaming32.bingo.gametest;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.gaming32.bingo.datagen.goal.GoalIds;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static io.github.gaming32.bingo.gametest.BingoClientGameTest.*;

// Player is at 0, 0, facing south (towards +Z)
@SuppressWarnings("UnstableApiUsage")
final class VeryEasyGoalTests {
    @TestGoal
    private static void testSimpleItemGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.APPLE, () -> {
            give(singleplayerContext, Items.APPLE);
        });
    }

    @TestGoal
    private static void testMultipleSimpleItemGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.STICK, () -> {
            give(singleplayerContext, Items.STICK, 64);
        });
    }

    @TestGoal
    private static void testPoppiesAndDandelionsGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.POPPIES_DANDELIONS, () -> {
            give(singleplayerContext, Items.POPPY, 25);
            give(singleplayerContext, Items.DANDELION, 25);
        });
    }

    @TestGoal
    private static void testPoppiesAndDandelionsOnlyPoppies(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.POPPIES_DANDELIONS, false, () -> {
            give(singleplayerContext, Items.POPPY, 25);
        });
    }

    @TestGoal
    private static void testSomeItemsFromTag(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.WOOL_COLORS, () -> {
            give(singleplayerContext, Items.WOOL.white());
            give(singleplayerContext, Items.WOOL.red());
            give(singleplayerContext, Items.WOOL.yellow());
            give(singleplayerContext, Items.WOOL.blue());
        });
    }

    @TestGoal
    private static void testLeafCube(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.LEAF_CUBE, () -> {
            fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), new BlockPos(3, PLAYER_Y + 3, 4), Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.AIR);
            give(singleplayerContext, Items.OAK_LEAVES);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testSleepInBed(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.SLEEP_IN_BED, () -> {
            setTime(singleplayerContext, ClockTimeMarkers.NIGHT);
            placeBed(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.BED.white());
            rotate(singleplayerContext, 0, 15);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testSomeEdibleItems(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.EDIBLE_ITEMS, () -> {
            give(singleplayerContext, Items.PORKCHOP);
            give(singleplayerContext, Items.BEEF);
            give(singleplayerContext, Items.MUTTON);
        });
    }

    @TestGoal
    private static void testSomeEdibleItemsMixCooked(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.EDIBLE_ITEMS, null, () -> {
            give(singleplayerContext, Items.PORKCHOP);
            give(singleplayerContext, Items.COOKED_PORKCHOP);
            if (getGoalProgress(singleplayerContext).progress() != 1) {
                throw new IllegalStateException("Goal progress should be 1");
            }
            give(singleplayerContext, Items.BEEF);
        });
    }

    @TestGoal
    private static void testBreedMobPair(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.BREED_MOB_PAIR, () -> {
            give(singleplayerContext, Items.WHEAT, 2);
            rotate(singleplayerContext, 0, 15);

            Cow cow = summon(singleplayerContext, EntityTypes.COW, new BlockPos(0, PLAYER_Y, 1));
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            teleport(singleplayerContext, cow, new BlockPos(-2, PLAYER_Y, 1));
            summon(singleplayerContext, EntityTypes.COW, new BlockPos(0, PLAYER_Y, 1));
            singleplayerContext.getConnection().waitForClientboundEntityUpdates(EntityTypes.COW);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);

            singleplayerContext.getServer().waitFor(_ -> Iterables.size(singleplayerContext.getConnection().getServerLevel().getAllEntities()) > 3);
        });
    }

    @TestGoal
    private static void testCrouchDistance(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.CROUCH_DISTANCE, () -> {
            Vec3 originalPos = singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().position());
            addEffect(singleplayerContext, MobEffects.SPEED, 64);
            context.getInput().holdKey(InputConstants.KEY_LSHIFT);
            context.getInput().holdKey(InputConstants.KEY_W);
            context.getInput().holdKey(InputConstants.KEY_D);
            try {
                int distanceNeeded = getGoalProgress(singleplayerContext).maxProgress() + 1;
                final double CONSERVATIVE_SNEAKING_SPEED = 1.7 / SharedConstants.TICKS_PER_SECOND;
                singleplayerContext.getServer().waitFor(
                    _ -> singleplayerContext.getConnection().getServerPlayer().position().distanceToSqr(originalPos) > distanceNeeded * distanceNeeded,
                    Mth.ceil(distanceNeeded / CONSERVATIVE_SNEAKING_SPEED)
                );
            } finally {
                context.getInput().releaseKey(InputConstants.KEY_LSHIFT);
                context.getInput().releaseKey(InputConstants.KEY_W);
                context.getInput().releaseKey(InputConstants.KEY_D);
            }
        });
    }

    @TestGoal
    private static void testDyeSign(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.DYE_SIGN, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), Blocks.OAK_SIGN);
            singleplayerContext.getServer().runOnServer(server -> {
                SignBlockEntity sign = singleplayerContext.getConnection().getServerLevel().getBlockEntity(new BlockPos(0, PLAYER_Y, 1), BlockEntityTypes.SIGN).orElseThrow();
                sign.updateText(text -> text.asMutable().setLine(1, Component.literal("Hello")).asImmutable(), SignTextSlot.FRONT);
            });
            give(singleplayerContext, Items.DYE.red());
            rotate(singleplayerContext, 0, 45);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testExtinguishCampfire(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.EXTINGUISH_CAMPFIRE, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), Blocks.CAMPFIRE);
            rotate(singleplayerContext, 0, 60);
            give(singleplayerContext, Items.STONE_SHOVEL);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testNeverPickUpCraftingTable(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.VeryEasy.NEVER_PICKUP_CRAFTING_TABLES, () -> {
            dropItem(singleplayerContext, new ItemStack(Items.CRAFTING_TABLE), new BlockPos(0, PLAYER_Y, 0));
            context.waitTick();
        });
    }

    @TestGoal
    private static void testNeverFish(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.VeryEasy.NEVER_FISH, () -> {
            give(singleplayerContext, Items.FISHING_ROD);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testBreakHoe(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.BREAK_HOE, () -> {
            ItemStack hoe = new ItemStack(Items.WOODEN_HOE);
            hoe.setDamageValue(hoe.getMaxDamage());
            give(singleplayerContext, hoe);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.DIRT);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testBounceOnBed(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.BOUNCE_ON_BED, () -> {
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 0));
            placeBed(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), Blocks.BED.white());
            singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerPlayer().getDeltaMovement().y > 0);
        });
    }

    @TestGoal
    private static void testHangPainting(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.HANG_PAINTING, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.DIRT);
            give(singleplayerContext, Items.PAINTING);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testFillComposter(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.FILL_COMPOSTER, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.COMPOSTER);
            give(singleplayerContext, Items.PUMPKIN_PIE, 7);
            singleplayerContext.getConnection().waitForClientboundPackets();
            for (int i = 0; i < 7; i++) {
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testDifferentSlabs(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.DIFFERENT_SLABS, () -> {
            for (Item item : List.of(Items.WOOL_SLAB.white(), Items.OAK_SLAB, Items.SPRUCE_SLAB, Items.BIRCH_SLAB)) {
                give(singleplayerContext, item);
            }
        });
    }

    @TestGoal
    private static void testDifferentStairs(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.DIFFERENT_STAIRS, () -> {
            for (Item item : List.of(Items.WOOL_STAIRS.white(), Items.OAK_STAIRS, Items.SPRUCE_STAIRS, Items.BIRCH_STAIRS)) {
                give(singleplayerContext, item);
            }
        });
    }
}
