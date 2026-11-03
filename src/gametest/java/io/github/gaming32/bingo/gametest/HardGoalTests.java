package io.github.gaming32.bingo.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.gaming32.bingo.datagen.goal.GoalIds;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.gaming32.bingo.gametest.BingoClientGameTest.*;

// Player is at 0, 0, facing south (towards +Z)
@SuppressWarnings("UnstableApiUsage")
final class HardGoalTests {
    @TestGoal
    private static void testPoplarLog(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.POPLAR_LOG, () -> {
            give(singleplayerContext, Items.POPLAR_LOG, 32);
        });
    }

    @TestGoal
    private static void testPoplarWood(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.POPLAR_WOOD, () -> {
            give(singleplayerContext, Items.POPLAR_WOOD, 20);
        });
    }

    @TestGoal
    private static void testStrippedPoplarLog(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.STRIPPED_POPLAR_LOG, () -> {
            give(singleplayerContext, Items.STRIPPED_POPLAR_LOG, 20);
        });
    }

    @TestGoal
    private static void testStrippedPoplarWood(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.STRIPPED_POPLAR_WOOD, () -> {
            give(singleplayerContext, Items.STRIPPED_POPLAR_WOOD, 20);
        });
    }

    @TestGoal
    private static void testDifferentExplorerMaps(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.DIFFERENT_EXPLORER_MAPS, () -> {
            give(singleplayerContext, Items.OCEAN_MONUMENT_MAP);
            give(singleplayerContext, Items.WOODLAND_MANSION_MAP);
            give(singleplayerContext, Items.ABANDONED_CAMP_MAP);
        });
    }

    @TestGoal
    private static void testDifferentExplorerMapsBlankMaps(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.DIFFERENT_EXPLORER_MAPS, false, () -> {
            give(singleplayerContext, Items.OCEAN_MONUMENT_MAP);
            give(singleplayerContext, Items.MAP);
            give(singleplayerContext, Items.FILLED_MAP);
        });
    }

    @TestGoal
    private static void testHugeFungusInOverworld(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.HUGE_FUNGUS_IN_OVERWORLD, () -> {
            growHugeFungus(context, singleplayerContext, new BlockPos(0, PLAYER_Y, 1), 53);
        });
    }

    @TestGoal(dimension = TestDimension.THE_NETHER)
    private static void testHugeFungusInOverworldInNether(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.HUGE_FUNGUS_IN_OVERWORLD, false, () -> {
            growHugeFungus(context, singleplayerContext, new BlockPos(0, 129, 1), 17);
        });
    }

    @TestGoal
    private static void testIceOnMagma(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.ICE_ON_MAGMA, () -> {
            placeOnTop(context, singleplayerContext, Blocks.MAGMA_BLOCK, Items.ICE);
        });
    }

    @TestGoal
    private static void testIceOnMagmaWrongWayRound(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.ICE_ON_MAGMA, false, () -> {
            placeOnTop(context, singleplayerContext, Blocks.ICE, Items.MAGMA_BLOCK);
        });
    }

    private static void placeOnTop(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Block bottom, Item top) {
        setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), bottom);
        give(singleplayerContext, top);
        rotate(singleplayerContext, 0, 35);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
    }

    @TestGoal
    private static void testStairwayToHeaven(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.STAIRWAY_TO_HEAVEN, () -> {
            standOnStairway(context, singleplayerContext, -1);
        });
    }

    @TestGoal
    private static void testStairwayToHeavenWithGap(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.STAIRWAY_TO_HEAVEN, false, () -> {
            standOnStairway(context, singleplayerContext, 190);
        });
    }

    private static void standOnStairway(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, int gapIndex) {
        // Straight stairway from the ground at z=379 up to the height limit at z=0
        int length = 320 - PLAYER_Y;
        try {
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerLevel level = singleplayerContext.getConnection().getServerLevel();
                BlockState stairs = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
                for (int i = 0; i < length; i++) {
                    if (i != gapIndex) {
                        BlockPos pos = new BlockPos(0, 319 - i, i);
                        fill(level, pos, pos, stairs);
                    }
                }
                ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
                teleport(level, player, new Vec3(0.5, 320, 0.25), 0, 0);
            });
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.waitTicks(30);
        } finally {
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerLevel level = singleplayerContext.getConnection().getServerLevel();
                for (int i = 0; i < length; i++) {
                    BlockPos pos = new BlockPos(0, 319 - i, i);
                    fill(level, pos, pos, Blocks.AIR.defaultBlockState());
                }
            });
        }
    }

    @TestGoal
    private static void testFullIronMob(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.FULL_IRON_MOB, () -> {
            killMobWearingArmor(singleplayerContext, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        });
    }

    @TestGoal
    private static void testFullIronMobMissingBoots(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.FULL_IRON_MOB, false, () -> {
            killMobWearingArmor(singleplayerContext, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.AIR);
        });
    }

    private static void killMobWearingArmor(TestSingleplayerContext singleplayerContext, Item helmet, Item chestplate, Item leggings, Item boots) {
        Zombie zombie = summon(singleplayerContext, EntityTypes.ZOMBIE, new BlockPos(0, PLAYER_Y, 2));
        singleplayerContext.getServer().runOnServer(_ -> {
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmet));
            zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chestplate));
            zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(leggings));
            zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots));
        });
        damageByPlayer(singleplayerContext, zombie, 1000);
    }

    @TestGoal
    private static void testLevel10Enchant(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.LEVEL_10_ENCHANT, () -> {
            setUpEnchantingTable(singleplayerContext);
            enchantItem(context, singleplayerContext, new ItemStack(Items.IRON_SWORD), 2);
        });
    }

    @TestGoal
    private static void testLevel10EnchantLowLevel(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.LEVEL_10_ENCHANT, false, () -> {
            setUpEnchantingTable(singleplayerContext);
            int cost = enchantItem(context, singleplayerContext, new ItemStack(Items.IRON_SWORD), 0);
            if (cost >= 10) {
                throw new IllegalStateException("Expected the first enchantment to cost less than 10 levels, but it cost " + cost);
            }
        });
    }

    @TestGoal
    private static void testEnchant5Items(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.ENCHANT_5_ITEMS, () -> {
            setUpEnchantingTable(singleplayerContext);
            for (Item item : List.of(Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HELMET)) {
                enchantItem(context, singleplayerContext, new ItemStack(item), 0);
            }
        });
    }

    private static void setUpEnchantingTable(TestSingleplayerContext singleplayerContext) {
        BlockPos tablePos = new BlockPos(0, PLAYER_Y, 3);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                // Leave a gap in front for the player to see the table through
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 2 && !(dz == -2 && Math.abs(dx) <= 1)) {
                    fill(singleplayerContext, tablePos.offset(dx, 0, dz), tablePos.offset(dx, 1, dz), Blocks.BOOKSHELF);
                }
            }
        }
        setBlock(singleplayerContext, tablePos, Blocks.ENCHANTING_TABLE);
        singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().giveExperienceLevels(100));
    }

    // Returns the level cost of the enchantment
    private static int enchantItem(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, ItemStack item, int button) {
        rotate(singleplayerContext, 0, 26);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        context.waitForScreen(EnchantmentScreen.class);
        singleplayerContext.getServer().runOnServer(_ -> {
            EnchantmentMenu menu = (EnchantmentMenu) singleplayerContext.getConnection().getServerPlayer().containerMenu;
            menu.getSlot(0).set(item);
            menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
            menu.broadcastChanges();
        });
        singleplayerContext.getConnection().waitForClientboundPackets();
        int cost = context.computeOnClient(client -> ((EnchantmentMenu) Objects.requireNonNull(client.player).containerMenu).costs[button]);
        if (cost <= 0) {
            throw new IllegalStateException("Enchantment option " + button + " is not available");
        }
        context.runOnClient(client -> Objects.requireNonNull(client.gameMode).handleInventoryButtonClick(Objects.requireNonNull(client.player).containerMenu.containerId, button));
        singleplayerContext.getConnection().waitForServerboundPackets();
        singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().closeContainer());
        singleplayerContext.getConnection().waitForClientboundPackets();
        return cost;
    }

    @TestGoal
    private static void testNeverUseBuckets(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Hard.NEVER_USE_BUCKETS, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), Blocks.WATER);
            give(singleplayerContext, Items.BUCKET);
            rotate(singleplayerContext, 0, 45);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testVoidDeathToEntity(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.VOID_DEATH_TO_ENTITY, () -> {
            Zombie zombie = summon(singleplayerContext, EntityTypes.ZOMBIE, new BlockPos(0, PLAYER_Y, 2));
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
                zombie.setNoAi(true);
                zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                if (!zombie.doHurtTarget(player.level(), player)) {
                    throw new IllegalStateException("Zombie failed to hurt player");
                }
                // Don't despawn when the player is far away in the void
                zombie.setPersistenceRequired();
                // Die quickly, before the zombie stops being credited with the kill
                player.setHealth(4);
                player.getFoodData().setFoodLevel(10);
                player.getFoodData().setSaturation(0);
            });
            teleport(singleplayerContext, new BlockPos(0, -130, 0));
            waitForGameEnd(singleplayerContext, 100);
        });
    }

    @TestGoal
    private static void testMegaJungleTree(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.MEGA_JUNGLE_TREE, () -> {
            try {
                fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 2), new BlockPos(1, PLAYER_Y, 3), Blocks.JUNGLE_SAPLING);
                give(singleplayerContext, Items.BONE_MEAL, 64);
                rotate(singleplayerContext, 0, 31);
                singleplayerContext.getConnection().waitForClientboundPackets();
                context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                try {
                    singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, PLAYER_Y, 2)).is(Blocks.JUNGLE_LOG));
                } finally {
                    context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                }
                singleplayerContext.getConnection().waitForServerboundPackets();
            } finally {
                fill(singleplayerContext, new BlockPos(-12, PLAYER_Y + 16, -10), new BlockPos(13, PLAYER_Y + 48, 15), Blocks.AIR);
            }
        });
    }

    @TestGoal
    private static void testDestroySpawner(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.DESTROY_SPAWNER, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.SPAWNER);
            give(singleplayerContext, Items.DIAMOND_PICKAXE);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
            try {
                singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, PLAYER_Y + 1, 1)).isAir(), 200);
            } finally {
                context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
            }
        });
    }

    @TestGoal
    private static void testDieToVillager(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.DIE_TO_VILLAGER, () -> {
            // Villager wearing thorns armor (e.g. from a dispenser)
            Villager villager = summon(singleplayerContext, EntityTypes.VILLAGER, new BlockPos(0, PLAYER_Y, 1));
            List<ItemStack> armor = List.of(
                enchantedItem(singleplayerContext, Items.IRON_HELMET, Enchantments.THORNS, 3),
                enchantedItem(singleplayerContext, Items.IRON_CHESTPLATE, Enchantments.THORNS, 3),
                enchantedItem(singleplayerContext, Items.IRON_LEGGINGS, Enchantments.THORNS, 3),
                enchantedItem(singleplayerContext, Items.IRON_BOOTS, Enchantments.THORNS, 3)
            );
            singleplayerContext.getServer().runOnServer(_ -> {
                villager.setNoAi(true);
                villager.setItemSlot(EquipmentSlot.HEAD, armor.get(0));
                villager.setItemSlot(EquipmentSlot.CHEST, armor.get(1));
                villager.setItemSlot(EquipmentSlot.LEGS, armor.get(2));
                villager.setItemSlot(EquipmentSlot.FEET, armor.get(3));
            });
            setHealth(singleplayerContext, 1);
            singleplayerContext.getConnection().waitForClientboundEntityUpdates(EntityTypes.VILLAGER);
            for (int i = 0; i < 20 && !isGameOver(singleplayerContext); i++) {
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
                singleplayerContext.getConnection().waitForServerboundPackets();
                context.waitTicks(11);
            }
        });
    }

    @TestGoal
    private static void testPacifist(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Hard.PACIFIST, () -> {
            damageByPlayer(singleplayerContext, summon(singleplayerContext, EntityTypes.CHICKEN, new BlockPos(0, PLAYER_Y, 2)), 1000);
        });
    }

    @TestGoal
    private static void testScaffoldingTower(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.SCAFFOLDING_TOWER, () -> {
            try {
                // Stand in a pit next to a scaffolding tower going from bedrock to the height limit, and break the bottom block
                fill(singleplayerContext, new BlockPos(0, BEDROCK_Y + 1, 0), new BlockPos(0, GRASS_Y, 0), Blocks.AIR);
                // Aim at a leg of the bottom scaffolding, since the middle of its sides are open
                teleport(singleplayerContext, new BlockPos(0, BEDROCK_Y + 1, 0), 41.3f, 59.3f);
                fill(singleplayerContext, new BlockPos(0, BEDROCK_Y + 1, 1), new BlockPos(0, 319, 1), Blocks.SCAFFOLDING.defaultBlockState().setValue(ScaffoldingBlock.DISTANCE, 0));
                singleplayerContext.getConnection().waitForClientboundPackets();
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
                singleplayerContext.getConnection().waitForServerboundPackets();
                if (!singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, BEDROCK_Y + 1, 1)).isAir())) {
                    throw new IllegalStateException("Bottom scaffolding was not broken");
                }
            } finally {
                restoreGround(singleplayerContext, new BlockPos(0, BEDROCK_Y + 1, 0), new BlockPos(0, 319, 1));
            }
        });
    }

    @TestGoal
    private static void test6x6Scaffolding(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard._6X6SCAFFOLDING, () -> {
            // An arch with legs at x=-3 and x=4, with the top at y=PLAYER_Y+6. The block at x=0 at the top is placed by the player.
            BlockState scaffolding = Blocks.SCAFFOLDING.defaultBlockState();
            fill(singleplayerContext, new BlockPos(-3, PLAYER_Y, 3), new BlockPos(-3, PLAYER_Y + 6, 3), scaffolding.setValue(ScaffoldingBlock.DISTANCE, 0));
            fill(singleplayerContext, new BlockPos(4, PLAYER_Y, 3), new BlockPos(4, PLAYER_Y + 6, 3), scaffolding.setValue(ScaffoldingBlock.DISTANCE, 0));
            for (int x = -2; x <= 3; x++) {
                if (x != 0) {
                    setBlock(singleplayerContext, new BlockPos(x, PLAYER_Y + 6, 3), scaffolding.setValue(ScaffoldingBlock.DISTANCE, Math.min(x + 3, 4 - x)));
                }
            }
            give(singleplayerContext, Items.SCAFFOLDING);
            teleport(singleplayerContext, new BlockPos(-2, PLAYER_Y + 7, 3), -90, 58);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            if (!singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, PLAYER_Y + 6, 3)).is(Blocks.SCAFFOLDING))) {
                throw new IllegalStateException("Scaffolding was not placed in the right position");
            }
        });
    }

    @TestGoal
    private static void testBurnMendingBook(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.BURN_MENDING_BOOK, () -> {
            throwBookIntoLava(context, singleplayerContext, Enchantments.MENDING);
        });
    }

    @TestGoal
    private static void testBurnMendingBookWrongEnchantment(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.BURN_MENDING_BOOK, false, () -> {
            throwBookIntoLava(context, singleplayerContext, Enchantments.UNBREAKING);
        });
    }

    private static void throwBookIntoLava(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, ResourceKey<Enchantment> enchantment) {
        fill(singleplayerContext, new BlockPos(-2, PLAYER_Y - 1, 1), new BlockPos(2, PLAYER_Y - 1, 4), Blocks.LAVA);
        ItemStack book = singleplayerContext.getServer().computeOnServer(server ->
            EnchantmentHelper.createBook(new EnchantmentInstance(server.registryAccess().getOrThrow(enchantment), 1))
        );
        give(singleplayerContext, book);
        rotate(singleplayerContext, 0, 20);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressKey(options -> options.keyDrop);
        singleplayerContext.getConnection().waitForServerboundPackets();
        singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypes.ITEM, _ -> true).isEmpty(), 100);
    }

    @TestGoal
    private static void testDirtNetherrackEndStone(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.DIRT_NETHERRACK_END_STONE, () -> {
            giveSplitStacks(singleplayerContext, getGoalNameIntArg(singleplayerContext, 0), Items.DIRT, Items.NETHERRACK, Items.END_STONE);
        });
    }

    @TestGoal
    private static void testDirtNetherrackEndStoneNotEnough(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.DIRT_NETHERRACK_END_STONE, false, () -> {
            int count = getGoalNameIntArg(singleplayerContext, 0);
            giveSplitStacks(singleplayerContext, count, Items.DIRT, Items.NETHERRACK);
            giveSplitStacks(singleplayerContext, count - 1, Items.END_STONE);
        });
    }

    @TestGoal
    private static void testTntSulfurCube(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.TNT_SULFUR_CUBE, () -> {
            SulfurCube sulfurCube = summon(singleplayerContext, EntityTypes.SULFUR_CUBE, new BlockPos(0, PLAYER_Y, 1));
            singleplayerContext.getServer().runOnServer(_ -> {
                sulfurCube.setNoAi(true);
                sulfurCube.setBaby(false);
            });
            give(singleplayerContext, Items.TNT);
            rotate(singleplayerContext, 0, 54);
            singleplayerContext.getConnection().waitForClientboundEntityUpdates(EntityTypes.SULFUR_CUBE);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testFinishOnBlazeSpawner(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.FINISH_ON_BLAZE_SPAWNER, () -> {
            standOnSpawner(context, singleplayerContext, EntityTypes.BLAZE);
        });
    }

    @TestGoal
    private static void testFinishOnBlazeSpawnerZombieSpawner(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.FINISH_ON_BLAZE_SPAWNER, false, () -> {
            standOnSpawner(context, singleplayerContext, EntityTypes.ZOMBIE);
        });
    }

    private static void standOnSpawner(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, EntityType<?> type) {
        teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 0));
        setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), Blocks.SPAWNER);
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            level.getBlockEntity(new BlockPos(0, PLAYER_Y, 0), BlockEntityTypes.MOB_SPAWNER).orElseThrow().setEntityId(type, level.getRandom());
        });
        context.waitTicks(30);
    }

    @TestGoal
    private static void testStatusEffects(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.STATUS_EFFECTS, () -> {
            for (Holder<MobEffect> effect : HARMLESS_EFFECTS.subList(0, getGoalNameIntArg(singleplayerContext, 0))) {
                addEffect(singleplayerContext, effect, 0);
            }
        });
    }

    @TestGoal
    private static void testStatusEffectsUpgraded(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        // Upgrading an effect doesn't make it count twice
        testGoal(context, singleplayerContext, GoalIds.Hard.STATUS_EFFECTS, false, () -> {
            for (Holder<MobEffect> effect : HARMLESS_EFFECTS.subList(0, getGoalNameIntArg(singleplayerContext, 0) - 1)) {
                addEffect(singleplayerContext, effect, 0);
            }
            addEffect(singleplayerContext, HARMLESS_EFFECTS.getFirst(), 1);
        });
    }

    @TestGoal
    private static void testTameWolfVariants(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.TAME_WOLF_VARIANTS, () -> {
            tameWolves(singleplayerContext, WOLF_VARIANTS.subList(0, getGoalNameIntArg(singleplayerContext, 0)));
        });
    }

    @TestGoal
    private static void testTameWolfVariantsRepeated(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.TAME_WOLF_VARIANTS, false, () -> {
            tameWolves(singleplayerContext, List.of(WolfVariants.PALE, WolfVariants.WOODS, WolfVariants.WOODS, WolfVariants.WOODS, WolfVariants.PALE));
        });
    }

    @TestGoal
    private static void testRideStriderInOverworld(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.RIDE_STRIDER_IN_OVERWORLD, () -> {
            mount(singleplayerContext, summon(singleplayerContext, EntityTypes.STRIDER, new BlockPos(0, PLAYER_Y, 2)));
        });
    }

    @TestGoal(dimension = TestDimension.THE_NETHER)
    private static void testRideStriderInNether(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.RIDE_STRIDER_IN_OVERWORLD, false, () -> {
            mount(singleplayerContext, summon(singleplayerContext, EntityTypes.STRIDER, new BlockPos(0, 128, 2)));
        });
    }

    @TestGoal
    private static void testKillWarden(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.KILL_WARDEN, () -> {
            Warden warden = summon(singleplayerContext, EntityTypes.WARDEN, new BlockPos(0, PLAYER_Y, 3));
            singleplayerContext.getServer().runOnServer(_ -> warden.setHealth(1));
            damageByPlayer(singleplayerContext, warden, 2);
        });
    }

    @TestGoal
    private static void testCrossbowDifferentMobs(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.CROSSBOW_DIFFERENT_MOBS, () -> {
            int count = getGoalNameIntArg(singleplayerContext, 0);
            shootMobsWithCrossbow(context, singleplayerContext, List.of(EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.HUSK, EntityTypes.STRAY).subList(0, count));
        });
    }

    @TestGoal
    private static void testCrossbowSameMobs(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.CROSSBOW_DIFFERENT_MOBS, false, () -> {
            shootMobsWithCrossbow(context, singleplayerContext, List.of(EntityTypes.ZOMBIE, EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.SKELETON));
        });
    }

    // Lines the mobs up in front of the player, and shoots them all with one arrow from a Piercing crossbow
    private static void shootMobsWithCrossbow(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, List<? extends EntityType<? extends Mob>> types) {
        // Undead mobs burn in daylight
        setTime(singleplayerContext, ClockTimeMarkers.NIGHT);
        List<Mob> mobs = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            Mob mob = summon(singleplayerContext, types.get(i), new BlockPos(0, PLAYER_Y, 3 + i * 2));
            singleplayerContext.getServer().runOnServer(_ -> {
                mob.setNoAi(true);
                mob.setHealth(1);
                // A baby zombie is too short to be hit
                if (mob instanceof Zombie zombie) {
                    zombie.setBaby(false);
                }
            });
            mobs.add(mob);
        }
        ItemStack crossbow = enchantedItem(singleplayerContext, Items.CROSSBOW, Enchantments.PIERCING, 4);
        crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(List.of(new ItemStack(Items.ARROW))));
        give(singleplayerContext, crossbow);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        singleplayerContext.getServer().waitFor(_ -> mobs.stream().noneMatch(Mob::isAlive), 40);
    }

    @TestGoal
    private static void testFroglight(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Hard.FROGLIGHT, () -> {
            give(singleplayerContext, Items.PEARLESCENT_FROGLIGHT);
        });
    }
}
