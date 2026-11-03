package io.github.gaming32.bingo.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.gaming32.bingo.datagen.goal.GoalIds;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.ShelfMushroomBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.state.properties.PotentSulfurState;

import java.util.List;
import java.util.Objects;

import static io.github.gaming32.bingo.gametest.BingoClientGameTest.*;

// Player is at 0, 0, facing south (towards +Z)
@SuppressWarnings("UnstableApiUsage")
final class MediumGoalTests {
    @TestGoal
    private static void testLaunchedByGeyser(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.LAUNCHED_BY_GEYSER, () -> {
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 3, 0));
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), Blocks.LAVA);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 0), Blocks.POTENT_SULFUR.defaultBlockState().setValue(PotentSulfurBlock.STATE, PotentSulfurState.CONTINUOUS));
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 2, 0), Blocks.WATER);
            context.waitTicks(20);
        });
    }

    @TestGoal
    private static void testFireCharge(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.FIRE_CHARGE, () -> {
            give(singleplayerContext, Items.FIRE_CHARGE, 64);
        });
    }

    @TestGoal
    private static void testBounceOnShelfMushroom(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.BOUNCE_ON_SHELF_MUSHROOM, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), Blocks.POPLAR_LOG);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), Blocks.SHELF_MUSHROOM.defaultBlockState().setValue(ShelfMushroomBlock.FACING, Direction.NORTH).setValue(ShelfMushroomBlock.AGE, 1));
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 3, 0));
            singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerPlayer().getDeltaMovement().y > 0);
        });
    }

    @TestGoal
    private static void testKillHostileOnCushion(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.KILL_HOSTILE_ON_CUSHION, () -> {
            mount(singleplayerContext, summon(singleplayerContext, EntityTypes.CUSHION, new BlockPos(0, PLAYER_Y, 0)));
            damageByPlayer(singleplayerContext, summon(singleplayerContext, EntityTypes.ZOMBIE, new BlockPos(0, PLAYER_Y, 2)), 1000);
        });
    }

    @TestGoal
    private static void testKillHostileNotOnCushion(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.KILL_HOSTILE_ON_CUSHION, false, () -> {
            summon(singleplayerContext, EntityTypes.CUSHION, new BlockPos(0, PLAYER_Y, 1));
            damageByPlayer(singleplayerContext, summon(singleplayerContext, EntityTypes.ZOMBIE, new BlockPos(0, PLAYER_Y, 2)), 1000);
        });
    }

    @TestGoal
    private static void testPoplarLeaves(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.ALL_POPLAR_LEAVES, () -> {
            give(singleplayerContext, Items.RED_POPLAR_LEAVES);
            give(singleplayerContext, Items.ORANGE_POPLAR_LEAVES);
            give(singleplayerContext, Items.YELLOW_POPLAR_LEAVES);
        });
    }

    @TestGoal
    private static void testPoplarLeavesMissingColor(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.ALL_POPLAR_LEAVES, false, () -> {
            give(singleplayerContext, Items.RED_POPLAR_LEAVES, 64);
            give(singleplayerContext, Items.ORANGE_POPLAR_LEAVES, 64);
        });
    }

    @TestGoal
    private static void testAbandonedCampMap(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.ABANDONED_CAMP_MAP, () -> {
            give(singleplayerContext, Items.ABANDONED_CAMP_MAP);
        });
    }

    @TestGoal
    private static void testDifferentCushions(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.DIFFERENT_CUSHIONS, () -> {
            for (DyeColor color : List.of(DyeColor.WHITE, DyeColor.RED, DyeColor.YELLOW, DyeColor.BLUE, DyeColor.GREEN)) {
                give(singleplayerContext, Items.CUSHION.pick(color));
            }
        });
    }

    @TestGoal
    private static void testPottedCactus(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.POTTED_CACTUS, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), Blocks.FLOWER_POT);
            give(singleplayerContext, Items.CACTUS);
            rotate(singleplayerContext, 0, 55);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testShootTntMinecart(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.SHOOT_TNT_MINECART, () -> {
            addEffect(singleplayerContext, MobEffects.RESISTANCE, 4);
            summon(singleplayerContext, EntityTypes.TNT_MINECART, new BlockPos(0, PLAYER_Y, 4));
            give(singleplayerContext, enchantedItem(singleplayerContext, Items.BOW, Enchantments.FLAME, 1));
            give(singleplayerContext, Items.ARROW);
            rotate(singleplayerContext, 0, 18);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().holdMouseFor(InputConstants.MOUSE_BUTTON_RIGHT, 25);
            singleplayerContext.getConnection().waitForServerboundPackets();
            singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypes.TNT_MINECART, _ -> true).isEmpty(), 100);
        });
    }

    @TestGoal
    private static void testNeverWearArmor(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Medium.NEVER_WEAR_ARMOR, () -> {
            give(singleplayerContext, Items.LEATHER_HELMET);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testSkeletonBow(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.SKELETON_BOW, () -> {
            Skeleton skeleton = summon(singleplayerContext, EntityTypes.SKELETON, new BlockPos(0, PLAYER_Y, 2));
            singleplayerContext.getServer().runOnServer(_ -> {
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                skeleton.setDropChance(EquipmentSlot.MAINHAND, 2);
            });
            damageByPlayer(singleplayerContext, skeleton, 1000);
            pickUpDroppedItem(context, singleplayerContext, Items.BOW);
        });
    }

    @TestGoal
    private static void testSkeletonBowDroppedByPlayer(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.SKELETON_BOW, false, () -> {
            summon(singleplayerContext, EntityTypes.SKELETON, new BlockPos(5, PLAYER_Y, 5));
            dropItem(singleplayerContext, new ItemStack(Items.BOW), new BlockPos(0, PLAYER_Y, 2));
            pickUpDroppedItem(context, singleplayerContext, Items.BOW);
        });
    }

    @TestGoal
    private static void testKillSelfWithArrow(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.KILL_SELF_WITH_ARROW, () -> {
            give(singleplayerContext, Items.BOW);
            give(singleplayerContext, Items.ARROW);
            rotate(singleplayerContext, 0, -90);
            setHealth(singleplayerContext, 1);
            // Stop natural regeneration
            singleplayerContext.getServer().runOnServer(_ -> {
                FoodData foodData = singleplayerContext.getConnection().getServerPlayer().getFoodData();
                foodData.setFoodLevel(10);
                foodData.setSaturation(0);
            });
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().holdMouseFor(InputConstants.MOUSE_BUTTON_RIGHT, 6);
            singleplayerContext.getConnection().waitForServerboundPackets();
            waitForGameEnd(singleplayerContext, 100);
        });
    }

    @TestGoal
    private static void testWhileTryingToEscape(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.WHILE_TRYING_TO_ESCAPE, () -> {
            // Deep enough that the zombie's knockback doesn't lift the player's head out of the water
            fill(singleplayerContext, new BlockPos(-1, PLAYER_Y, -1), new BlockPos(1, PLAYER_Y + 5, 1), Blocks.GLASS);
            fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), new BlockPos(0, PLAYER_Y + 4, 0), Blocks.WATER);
            Zombie zombie = summon(singleplayerContext, EntityTypes.ZOMBIE, new BlockPos(0, PLAYER_Y, 3));
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
                zombie.setNoAi(true);
                zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                if (!zombie.doHurtTarget(player.level(), player)) {
                    throw new IllegalStateException("Zombie failed to hurt player");
                }
                player.setHealth(1);
                player.setAirSupply(0);
                // Stop natural regeneration
                player.getFoodData().setFoodLevel(10);
                player.getFoodData().setSaturation(0);
            });
            waitForGameEnd(singleplayerContext, 100);
        });
    }

    @TestGoal
    private static void testFinishOnTop(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.FINISH_ON_TOP, () -> {
            setBlock(singleplayerContext, new BlockPos(0, 319, 0), Blocks.STONE);
            teleport(singleplayerContext, new BlockPos(0, 320, 0));
            context.waitTicks(30);
        });
    }

    @TestGoal
    private static void testSizedNetherPortal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.SIZED_NETHER_PORTAL, () -> {
            buildAndLightNetherPortal(context, singleplayerContext, getGoalNameIntArg(singleplayerContext, 0), getGoalNameIntArg(singleplayerContext, 1));
        });
    }

    @TestGoal
    private static void testSizedNetherPortalWrongSize(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.SIZED_NETHER_PORTAL, false, () -> {
            buildAndLightNetherPortal(context, singleplayerContext, getGoalNameIntArg(singleplayerContext, 0) + 1, getGoalNameIntArg(singleplayerContext, 1));
        });
    }

    private static void buildAndLightNetherPortal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, int width, int height) {
        fill(singleplayerContext, new BlockPos(-1, PLAYER_Y - 1, 2), new BlockPos(width, PLAYER_Y + height, 2), Blocks.OBSIDIAN);
        fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 2), new BlockPos(width - 1, PLAYER_Y + height - 1, 2), Blocks.AIR);
        give(singleplayerContext, Items.FLINT_AND_STEEL);
        rotate(singleplayerContext, 0, 40);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        if (!singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, PLAYER_Y, 2)).is(Blocks.NETHER_PORTAL))) {
            throw new IllegalStateException("Nether portal was not lit");
        }
    }

    @TestGoal
    private static void testLeadOnRabbit(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.LEAD_ON_RABBIT, () -> {
            Rabbit rabbit = summon(singleplayerContext, EntityTypes.RABBIT, new BlockPos(0, PLAYER_Y, 1));
            singleplayerContext.getServer().runOnServer(_ -> rabbit.setNoAi(true));
            give(singleplayerContext, Items.LEAD);
            rotate(singleplayerContext, 0, 54);
            singleplayerContext.getConnection().waitForClientboundEntityUpdates(EntityTypes.RABBIT);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testHangMob(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.HANG_MOB, () -> {
            Pig pig = summon(singleplayerContext, EntityTypes.PIG, new BlockPos(0, PLAYER_Y, 1));
            give(singleplayerContext, Items.LEAD, 2);
            rotate(singleplayerContext, 0, 49);
            singleplayerContext.getConnection().waitForClientboundEntityUpdates(EntityTypes.PIG);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            if (!singleplayerContext.getServer().computeOnServer(_ -> pig.isLeashed())) {
                throw new IllegalStateException("Pig was not leashed");
            }

            // Stand on a pillar next to a floating fence, high enough above the pig that the pig will be lifted off the ground
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 9, 0), 0, 48);
            fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), new BlockPos(0, PLAYER_Y + 8, 0), Blocks.GLASS);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 9, 1), Blocks.OAK_FENCE);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            if (!singleplayerContext.getServer().computeOnServer(_ -> pig.getLeashHolder() instanceof LeashFenceKnotEntity)) {
                throw new IllegalStateException("Pig was not leashed to the fence");
            }

            singleplayerContext.getServer().waitFor(_ -> pig.getY() > PLAYER_Y + 1, 100);
            context.waitTicks(20);
        });
    }

    @TestGoal
    private static void testHangMobDigHole(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.HANG_MOB, () -> {
            hangMobOverHole(context, singleplayerContext, false);
        });
    }

    @TestGoal
    private static void testHangMobDigHoleSlowFalling(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.HANG_MOB, () -> {
            hangMobOverHole(context, singleplayerContext, true);
        });
    }

    @TestGoal
    private static void testHangMobPulledUpByFence(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.HANG_MOB, () -> {
            Pig pig = summon(singleplayerContext, EntityTypes.PIG, new BlockPos(0, PLAYER_Y, 1));
            give(singleplayerContext, Items.LEAD, 2);
            rotate(singleplayerContext, 0, 49);
            singleplayerContext.getConnection().waitForClientboundEntityUpdates(EntityTypes.PIG);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            if (!singleplayerContext.getServer().computeOnServer(_ -> pig.isLeashed())) {
                throw new IllegalStateException("Pig was not leashed");
            }

            // Put the pig at the bottom of a pit, close enough to the player at the edge that the lead is slack,
            // and float a fence high above it, so the pig is only lifted once the lead is attached to the fence
            fill(singleplayerContext, new BlockPos(-1, PLAYER_Y - 4, 2), new BlockPos(1, PLAYER_Y - 1, 4), Blocks.AIR);
            teleport(singleplayerContext, pig, new BlockPos(0, PLAYER_Y - 4, 3));
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), 0, -62.7f);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 5, 3), Blocks.OAK_FENCE);
            context.waitTicks(20);
            singleplayerContext.getConnection().waitForClientboundPackets();
            if (!singleplayerContext.getServer().computeOnServer(_ -> pig.isLeashed() && pig.onGround())) {
                throw new IllegalStateException("Pig was lifted before being attached to the fence");
            }
            if (isGameOver(singleplayerContext)) {
                throw new AssertionError("Goal was completed before the pig was attached to the fence");
            }

            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            if (!singleplayerContext.getServer().computeOnServer(_ -> pig.getLeashHolder() instanceof LeashFenceKnotEntity)) {
                throw new IllegalStateException("Pig was not leashed to the fence");
            }

            singleplayerContext.getServer().waitFor(_ -> pig.getY() > PLAYER_Y - 3, 100);
            context.waitTicks(20);
        });
    }

    @TestGoal
    private static void testHangMobOnGround(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.HANG_MOB, false, () -> {
            Pig pig = leashPigToFence(context, singleplayerContext, false);
            context.waitTicks(50);
            if (!singleplayerContext.getServer().computeOnServer(_ -> pig.isLeashed() && pig.onGround())) {
                throw new IllegalStateException("Pig is not leashed on the ground");
            }
        });
    }

    // Leashes a pig to a fence on the ground, then digs a hole under the pig so that it hangs from the fence
    private static void hangMobOverHole(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, boolean slowFalling) {
        Pig pig = leashPigToFence(context, singleplayerContext, slowFalling);
        fill(singleplayerContext, new BlockPos(-1, PLAYER_Y - 8, 2), new BlockPos(1, PLAYER_Y - 1, 4), Blocks.AIR);
        singleplayerContext.getServer().waitFor(_ -> pig.getY() < PLAYER_Y - 1, 100);
        context.waitTicks(60);
        if (!singleplayerContext.getServer().computeOnServer(_ -> pig.isLeashed() && !pig.onGround())) {
            throw new IllegalStateException("Pig is not hanging from the lead");
        }
    }

    // Leashes a pig on the ground to a fence next to it
    private static Pig leashPigToFence(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, boolean slowFalling) {
        Pig pig = summon(singleplayerContext, EntityTypes.PIG, new BlockPos(0, PLAYER_Y, 2));
        if (slowFalling) {
            singleplayerContext.getServer().runOnServer(_ -> pig.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, MobEffectInstance.INFINITE_DURATION)));
        }
        give(singleplayerContext, Items.LEAD, 2);
        rotate(singleplayerContext, 0, 30);
        singleplayerContext.getConnection().waitForClientboundEntityUpdates(EntityTypes.PIG);
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        if (!singleplayerContext.getServer().computeOnServer(_ -> pig.isLeashed())) {
            throw new IllegalStateException("Pig was not leashed");
        }

        setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), Blocks.OAK_FENCE);
        rotate(singleplayerContext, 0, 45);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        if (!singleplayerContext.getServer().computeOnServer(_ -> pig.getLeashHolder() instanceof LeashFenceKnotEntity)) {
            throw new IllegalStateException("Pig was not leashed to the fence");
        }
        return pig;
    }

    @TestGoal
    private static void testOneByOneHole(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.ONE_BY_ONE_HOLE, () -> {
            BlockPos top = new BlockPos(0, GRASS_Y, 0);
            BlockPos bottom = new BlockPos(0, BEDROCK_Y + 1, 0);
            try {
                fill(singleplayerContext, bottom, top, Blocks.AIR);
                teleport(singleplayerContext, bottom);
                context.waitTicks(30);
            } finally {
                restoreGround(singleplayerContext, bottom, top);
            }
        });
    }

    @TestGoal
    private static void testOneByOneHoleOpenArea(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.ONE_BY_ONE_HOLE, false, () -> {
            context.waitTicks(30);
        });
    }

    @TestGoal
    private static void testLeashDolphinToFence(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.LEASH_DOLPHIN_TO_FENCE, () -> {
            Dolphin dolphin = summon(singleplayerContext, EntityTypes.DOLPHIN, new BlockPos(0, PLAYER_Y, 1));
            singleplayerContext.getServer().runOnServer(_ -> dolphin.setNoAi(true));
            give(singleplayerContext, Items.LEAD, 2);
            rotate(singleplayerContext, 0, 53);
            singleplayerContext.getConnection().waitForClientboundEntityUpdates(EntityTypes.DOLPHIN);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            if (!singleplayerContext.getServer().computeOnServer(_ -> dolphin.isLeashed())) {
                throw new IllegalStateException("Dolphin was not leashed");
            }

            setBlock(singleplayerContext, new BlockPos(-1, PLAYER_Y + 1, 0), Blocks.OAK_FENCE);
            rotate(singleplayerContext, 90, 0);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testBreedMobs(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.BREED_MOBS, () -> {
            int count = getGoalProgress(singleplayerContext).maxProgress();
            for (int i = 0; i < count; i++) {
                Cow cow1 = summon(singleplayerContext, EntityTypes.COW, new BlockPos(i * 3 - 9, PLAYER_Y, 4));
                Cow cow2 = summon(singleplayerContext, EntityTypes.COW, new BlockPos(i * 3 - 9, PLAYER_Y, 5));
                singleplayerContext.getServer().runOnServer(_ -> {
                    ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
                    cow1.setInLove(player);
                    cow2.setInLove(player);
                });
            }
            waitForGameEnd(singleplayerContext, 600);
        });
    }

    @TestGoal
    private static void testKillMobs(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.KILL_MOBS, () -> {
            int count = getGoalProgress(singleplayerContext).maxProgress();
            for (int i = 0; i < count; i++) {
                damageByPlayer(singleplayerContext, summon(singleplayerContext, EntityTypes.CHICKEN, new BlockPos(0, PLAYER_Y, 2)), 1000);
            }
        });
    }

    @TestGoal
    private static void testKillWithCrystal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.KILL_WITH_CRYSTAL, () -> {
            addEffect(singleplayerContext, MobEffects.RESISTANCE, 4);
            EndCrystal crystal = summon(singleplayerContext, EntityTypes.END_CRYSTAL, new BlockPos(0, PLAYER_Y, 3));
            Zombie zombie = summon(singleplayerContext, EntityTypes.ZOMBIE, new BlockPos(1, PLAYER_Y, 3));
            damageByPlayer(singleplayerContext, crystal, 1);
            singleplayerContext.getServer().waitFor(_ -> !zombie.isAlive(), 20);
        });
    }

    @TestGoal
    private static void testKillHostileWithBerries(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.KILL_HOSTILE_WITH_BERRIES, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 3), Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE));
            Zombie zombie = summon(singleplayerContext, EntityTypes.ZOMBIE, new BlockPos(0, PLAYER_Y, 3));
            singleplayerContext.getServer().runOnServer(_ -> {
                // Make sure it's a normal zombie (not a baby or a chicken jockey) that's walking through the bush
                zombie.setBaby(false);
                Entity vehicle = zombie.getVehicle();
                if (vehicle != null) {
                    zombie.stopRiding();
                    vehicle.discard();
                }
                // Stop the zombie from burning in the sun
                zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                zombie.setHealth(1);
            });
            singleplayerContext.getServer().waitFor(_ -> !zombie.isAlive(), 200);
        });
    }

    @TestGoal
    private static void testGrowTreeWithBeeNest(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.GROW_TREE_WITH_BEE_NEST, () -> {
            // There's only a 5% chance that a tree grown near flowers has a bee nest, so keep trying
            BlockPos saplingPos = new BlockPos(0, PLAYER_Y, 2);
            rotate(singleplayerContext, 0, 31);
            // Use dirt rather than grass, so that missed bone meal doesn't grow grass that gets in the way
            fill(singleplayerContext, new BlockPos(-8, PLAYER_Y - 1, -6), new BlockPos(8, PLAYER_Y - 1, 10), Blocks.DIRT);
            for (int attempt = 0; attempt < 150 && !isGameOver(singleplayerContext); attempt++) {
                fill(singleplayerContext, new BlockPos(-8, PLAYER_Y, -6), new BlockPos(8, PLAYER_Y + 24, 10), Blocks.AIR);
                setBlock(singleplayerContext, saplingPos, Blocks.OAK_SAPLING);
                setBlock(singleplayerContext, new BlockPos(2, PLAYER_Y, 2), Blocks.POPPY);
                clearInventory(singleplayerContext);
                give(singleplayerContext, Items.BONE_MEAL, 64);
                singleplayerContext.getConnection().waitForClientboundPackets();
                context.waitFor(client -> Objects.requireNonNull(client.level).getBlockState(saplingPos).is(Blocks.OAK_SAPLING));
                for (int i = 0; i < 64 && !singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(saplingPos).is(BlockTags.LOGS)); i++) {
                    context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                    singleplayerContext.getConnection().waitForServerboundPackets();
                }
                if (!singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(saplingPos).is(BlockTags.LOGS))) {
                    throw new IllegalStateException("Tree did not grow");
                }
                context.waitTick();
            }
        });
    }

    @TestGoal
    private static void testAnchorInOverworld(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.ANCHOR_IN_OVERWORLD, () -> {
            addEffect(singleplayerContext, MobEffects.RESISTANCE, 4);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 1));
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testBounceOnSlime(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.BOUNCE_ON_SLIME, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), Blocks.SLIME_BLOCK);
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 4, 0));
            singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerPlayer().getDeltaMovement().y > 0);
        });
    }

    @TestGoal(dimension = TestDimension.THE_NETHER)
    private static void testGrowNetherFungus(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.GROW_NETHER_FUNGUS, () -> {
            growHugeFungus(context, singleplayerContext, new BlockPos(0, 129, 1), 17);
        });
    }

    @TestGoal
    private static void testNeverPlaceTorches(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Medium.NEVER_PLACE_TORCHES, () -> {
            placeTorchAtFeet(context, singleplayerContext, Items.TORCH);
        });
    }

    @TestGoal
    private static void testNeverPlaceTorchesSoulTorch(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Medium.NEVER_PLACE_TORCHES, () -> {
            placeTorchAtFeet(context, singleplayerContext, Items.SOUL_TORCH);
        });
    }

    private static void placeTorchAtFeet(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Item torch) {
        give(singleplayerContext, torch);
        rotate(singleplayerContext, 0, 90);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        if (!singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, PLAYER_Y, 0)).is(Block.byItem(torch)))) {
            throw new IllegalStateException("Torch was not placed");
        }
    }

    @TestGoal
    private static void testAllDifferentArmor(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.ALL_DIFFERENT_ARMOR, () -> {
            wearArmorThenEquipBoots(context, singleplayerContext, Items.LEATHER_HELMET, Items.IRON_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.DIAMOND_BOOTS);
        });
    }

    @TestGoal
    private static void testAllDifferentArmorSameMaterial(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.ALL_DIFFERENT_ARMOR, false, () -> {
            wearArmorThenEquipBoots(context, singleplayerContext, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.DIAMOND_BOOTS);
        });
    }

    @TestGoal
    private static void testSwapHotbarWithShelf(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.SWAP_HOTBAR_WITH_SHELF, () -> {
            setUpShelvesAndSwapHotbar(context, singleplayerContext, true);
        });
    }

    @TestGoal
    private static void testSwapHotbarWithShelfNotFull(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.SWAP_HOTBAR_WITH_SHELF, false, () -> {
            setUpShelvesAndSwapHotbar(context, singleplayerContext, false);
        });
    }

    private static void setUpShelvesAndSwapHotbar(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, boolean fillShelves) {
        fill(singleplayerContext, new BlockPos(-1, PLAYER_Y + 1, 2), new BlockPos(1, PLAYER_Y + 1, 2), Blocks.REDSTONE_BLOCK);
        for (int x = -1; x <= 1; x++) {
            setBlock(singleplayerContext, new BlockPos(x, PLAYER_Y + 1, 1), Blocks.OAK_SHELF.defaultBlockState().setValue(ShelfBlock.FACING, Direction.NORTH).setValue(ShelfBlock.POWERED, true));
        }
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            for (int x = -1; x <= 1; x++) {
                ShelfBlockEntity shelf = level.getBlockEntity(new BlockPos(x, PLAYER_Y + 1, 1), BlockEntityTypes.SHELF).orElseThrow();
                for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
                    if (fillShelves || slot != 0) {
                        shelf.setItem(slot, new ItemStack(Items.DIRT));
                    }
                }
            }
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
                player.getInventory().setItem(slot, new ItemStack(Items.STONE));
            }
            player.containerMenu.broadcastChanges();
        });
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        if (!singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().getInventory().getItem(Inventory.SELECTION_SIZE - 1).is(Items.DIRT))) {
            throw new IllegalStateException("Hotbar was not swapped");
        }
    }

    @TestGoal
    private static void testBasicMineralBlocks(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.BASIC_MINERAL_BLOCKS, () -> {
            buildMineralPillar(context, singleplayerContext, List.of(Blocks.DIAMOND_BLOCK, Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK));
        });
    }

    @TestGoal
    private static void testBasicMineralBlocksMissingBlock(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.BASIC_MINERAL_BLOCKS, false, () -> {
            buildMineralPillar(context, singleplayerContext, List.of(Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK));
        });
    }

    @TestGoal
    private static void testFinishByFreeFalling(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Medium.FINISH_BY_FREE_FALLING, () -> {
            // Fall down a shaft onto bedrock, with resistance to cancel the fall damage. Landing in water doesn't work, since
            // the fall is counted as ending a few blocks above the water when falling quickly.
            BlockPos bottom = new BlockPos(0, BEDROCK_Y + 1, 1);
            BlockPos top = new BlockPos(0, GRASS_Y, 1);
            try {
                fill(singleplayerContext, bottom, top, Blocks.AIR);
                addEffect(singleplayerContext, MobEffects.RESISTANCE, 4);
                teleport(singleplayerContext, new BlockPos(0, 320, 1));
                waitForGameEnd(singleplayerContext, 400);
            } finally {
                restoreGround(singleplayerContext, bottom, top);
            }
        });
    }

    @TestGoal
    private static void testVegetarian(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Medium.VEGETARIAN, () -> {
            eat(context, singleplayerContext, Items.COOKED_BEEF);
        });
    }

    @TestGoal
    private static void testVegetarianEatBread(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Medium.VEGETARIAN, false, () -> {
            eat(context, singleplayerContext, Items.BREAD);
        });
    }

    @TestGoal
    private static void testNeverCraftSticks(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Medium.NEVER_CRAFT_STICKS, () -> {
            singleplayerContext.getServer().runOnServer(_ -> {
                InventoryMenu menu = singleplayerContext.getConnection().getServerPlayer().inventoryMenu;
                menu.getSlot(InventoryMenu.CRAFT_SLOT_START).set(new ItemStack(Items.OAK_PLANKS));
                menu.getSlot(InventoryMenu.CRAFT_SLOT_START + 2).set(new ItemStack(Items.OAK_PLANKS));
                menu.broadcastChanges();
            });
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.runOnClient(client -> {
                LocalPlayer player = Objects.requireNonNull(client.player);
                if (!player.inventoryMenu.getSlot(InventoryMenu.RESULT_SLOT).getItem().is(Items.STICK)) {
                    throw new IllegalStateException("Crafting result is not sticks");
                }
                Objects.requireNonNull(client.gameMode).handleContainerInput(player.inventoryMenu.containerId, InventoryMenu.RESULT_SLOT, InputConstants.MOUSE_BUTTON_LEFT, ContainerInput.QUICK_MOVE, player);
            });
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }
}
