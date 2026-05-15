package io.github.gaming32.bingo.gametest;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import io.github.gaming32.bingo.datagen.goal.GoalIds;
import io.github.gaming32.bingo.ext.MinecraftServerExt;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.util.BingoUtil;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.FailablePredicate;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

// Player is at 0, 0, facing south (towards +Z)
@SuppressWarnings("UnstableApiUsage")
public class BingoClientGameTest implements FabricClientGameTest {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PLAYER_Y = -60;
    private static final List<String> failedTests = new ArrayList<>();
    private static boolean syncPacketReceived = false;

    @TestGoal
    private static void testSimpleItemGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.APPLE, () -> {
            singleplayerContext.getServer().runCommand("give @a apple");
        });
    }

    @TestGoal
    private static void testMultipleSimpleItemGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.STICK, () -> {
            singleplayerContext.getServer().runCommand("give @a stick 64");
        });
    }

    @TestGoal
    private static void testPoppiesAndDandelionsGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.POPPIES_DANDELIONS, () -> {
            singleplayerContext.getServer().runCommand("give @a poppy 25");
            singleplayerContext.getServer().runCommand("give @a dandelion 25");
        });
    }

    @TestGoal
    private static void testPoppiesAndDandelionsOnlyPoppies(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.POPPIES_DANDELIONS, false, () -> {
            singleplayerContext.getServer().runCommand("give @a poppy 25");
        });
    }

    @TestGoal
    private static void testSomeItemsFromTag(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.WOOL_COLORS, () -> {
            singleplayerContext.getServer().runCommand("give @a white_wool");
            singleplayerContext.getServer().runCommand("give @a red_wool");
            singleplayerContext.getServer().runCommand("give @a yellow_wool");
            singleplayerContext.getServer().runCommand("give @a blue_wool");
        });
    }

    @TestGoal
    private static void testLeafCube(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.LEAF_CUBE, () -> {
            singleplayerContext.getServer().runCommand("fill 0 " + PLAYER_Y + " 1 3 " + (PLAYER_Y + 3) + " 4 oak_leaves[persistent=true]");
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 1 air");
            singleplayerContext.getServer().runCommand("give @a oak_leaves");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testSleepInBed(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.SLEEP_IN_BED, () -> {
            singleplayerContext.getServer().runCommand("time set night");
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 1 white_bed[part=head]");
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 2 white_bed[part=foot]");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 15");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
            singleplayerContext.getServer().runCommand("time set day");
        });
    }

    @TestGoal
    private static void testSomeEdibleItems(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.EDIBLE_ITEMS, () -> {
            singleplayerContext.getServer().runCommand("give @a porkchop");
            singleplayerContext.getServer().runCommand("give @a beef");
            singleplayerContext.getServer().runCommand("give @a mutton");
        });
    }

    @TestGoal
    private static void testSomeEdibleItemsMixCooked(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.EDIBLE_ITEMS, null, () -> {
            singleplayerContext.getServer().runCommand("give @a porkchop");
            singleplayerContext.getServer().runCommand("give @a cooked_porkchop");
            singleplayerContext.getServer().runOnServer(server -> {
                if (getGoalProgress(server).progress() != 1) {
                    throw new IllegalStateException("Goal progress should be 1");
                }
            });
            singleplayerContext.getServer().runCommand("give @a beef");
        });
    }

    @TestGoal
    private static void testBreedMobPair(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.BREED_MOB_PAIR, () -> {
            singleplayerContext.getServer().runCommand("give @a wheat 2");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 15");

            singleplayerContext.getServer().runCommand("summon cow 0 " + PLAYER_Y + " 1");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getServer().runCommand("execute as @e[type=cow] at @s run tp @s ~-2 ~ ~");
            context.waitTicks(EntityType.COW.updateInterval()); // to send packets for the entity teleport

            singleplayerContext.getServer().runCommand("summon cow 0 " + PLAYER_Y + " 1");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);

            waitFor(context, singleplayerContext.getServer(), server -> Iterables.size(server.overworld().getAllEntities()) > 3);
        });
    }

    @TestGoal
    private static void testCrouchDistance(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.CROUCH_DISTANCE, () -> {
            Vec3 originalPos = singleplayerContext.getServer().computeOnServer(server -> getServerPlayer(server).position());
            singleplayerContext.getServer().runCommand("effect give @a speed infinite 64");
            context.getInput().holdKey(InputConstants.KEY_LSHIFT);
            context.getInput().holdKey(InputConstants.KEY_W);
            context.getInput().holdKey(InputConstants.KEY_D);
            try {
                int distanceNeeded = singleplayerContext.getServer().computeOnServer(server -> getGoalProgress(server).maxProgress()) + 1;
                final double CONSERVATIVE_SNEAKING_SPEED = 1.7 / SharedConstants.TICKS_PER_SECOND;
                waitFor(
                    context,
                    singleplayerContext.getServer(),
                    server -> getServerPlayer(server).position().distanceToSqr(originalPos) > distanceNeeded * distanceNeeded,
                    Mth.ceil(distanceNeeded / CONSERVATIVE_SNEAKING_SPEED)
                );
            } finally {
                context.getInput().releaseKey(InputConstants.KEY_LSHIFT);
                context.getInput().releaseKey(InputConstants.KEY_W);
                context.getInput().releaseKey(InputConstants.KEY_D);
                singleplayerContext.getServer().runCommand("effect clear @a");
            }
        });
    }

    @TestGoal
    private static void testDyeSign(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.DYE_SIGN, () -> {
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 1 oak_sign");
            singleplayerContext.getServer().runOnServer(server -> {
                SignBlockEntity sign = server.overworld().getBlockEntity(new BlockPos(0, PLAYER_Y, 1), BlockEntityType.SIGN).orElseThrow();
                sign.updateText(text -> text.setMessage(1, Component.literal("Hello")), true);
            });
            singleplayerContext.getServer().runCommand("give @a red_dye");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 45");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testExtinguishCampfire(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.EXTINGUISH_CAMPFIRE, () -> {
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 1 campfire");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 60");
            singleplayerContext.getServer().runCommand("give @a stone_shovel");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testNeverPickUpCraftingTable(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.VeryEasy.NEVER_PICKUP_CRAFTING_TABLES, () -> {
            singleplayerContext.getServer().runOnServer(server -> {
                ItemEntity item = new ItemEntity(server.overworld(), 0.5, PLAYER_Y, 0.5, new ItemStack(Items.CRAFTING_TABLE));
                server.overworld().addFreshEntity(item);
            });
            context.waitTick();
        });
    }

    @TestGoal
    private static void testNeverFish(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.VeryEasy.NEVER_FISH, () -> {
            singleplayerContext.getServer().runCommand("give @a fishing_rod");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testBreakHoe(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.BREAK_HOE, () -> {
            singleplayerContext.getServer().runCommand("give @a wooden_hoe[damage=" + new ItemStack(Items.WOODEN_HOE).getMaxDamage() + "]");
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 1 dirt");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testBounceOnBed(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.BOUNCE_ON_BED, () -> {
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~1 ~");
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 0 white_bed[part=head]");
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 1 white_bed[part=foot]");
            waitFor(context, singleplayerContext.getServer(), server -> getServerPlayer(server).getDeltaMovement().y > 0);
        });
    }

    @TestGoal
    private static void testHangPainting(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.HANG_PAINTING, () -> {
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 1 dirt");
            singleplayerContext.getServer().runCommand("give @a painting");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testFillComposter(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.FILL_COMPOSTER, () -> {
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 1 composter");
            singleplayerContext.getServer().runCommand("give @a pumpkin_pie 7");
            waitClientbound(context, singleplayerContext);
            for (int i = 0; i < 7; i++) {
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
            waitServerbound(context);
        });
    }

    @TestGoal(dimension = TestDimension.THE_NETHER)
    private static void testGrowTreeInNether(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.GROW_TREE_IN_NETHER, () -> {
            singleplayerContext.getServer().runCommand("execute in the_nether run setblock 0 128 1 dirt");
            singleplayerContext.getServer().runCommand("execute in the_nether run setblock 0 129 1 oak_sapling");
            singleplayerContext.getServer().runCommand("give @a bone_meal 64");
            waitClientbound(context, singleplayerContext);
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            try {
                waitFor(
                    context,
                    singleplayerContext.getServer(),
                    server -> Objects.requireNonNull(server.getLevel(Level.NETHER)).getBlockState(new BlockPos(0, 129, 1)).is(Blocks.OAK_LOG)
                );
            } finally {
                context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
        });
    }

    @TestGoal
    private static void testShootButton(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.SHOOT_BUTTON, () -> {
            singleplayerContext.getServer().runCommand("give @a bow");
            singleplayerContext.getServer().runCommand("give @a arrow");
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 0 oak_button[face=floor]");
            waitClientbound(context, singleplayerContext);
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            try {
                context.waitTicks(10);
                singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 90");
                waitClientbound(context, singleplayerContext);
                context.waitTicks(10);
            } finally {
                context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
            waitServerbound(context);
            context.waitTicks(10);
        });
    }

    @TestGoal
    private static void testEatEntireCake(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.EAT_ENTIRE_CAKE, () -> {
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ -30 15");
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 1 cake");
            singleplayerContext.getServer().runCommand("effect give @a hunger infinite 64");
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            try {
                waitFor(
                    context,
                    singleplayerContext.getServer(),
                    server -> server.overworld().getBlockState(new BlockPos(0, PLAYER_Y + 1, 1)).isAir(),
                    15 * SharedConstants.TICKS_PER_SECOND
                );
            } finally {
                context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                singleplayerContext.getServer().runCommand("effect clear @a");
            }
        });
    }

    @TestGoal
    private static void testFishTreasureAndJunk(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FISH_TREASURE_JUNK, () -> {
            // seed 16 gets a treasure and a junk item on the first two fishes
            singleplayerContext.getServer().runCommand("random reset " + BuiltInLootTables.FISHING.identifier() + " 16 false");
            singleplayerContext.getServer().runCommand("fill -10 " + PLAYER_Y + " -10 10 " + (PLAYER_Y + 6) + " 10 glass");
            singleplayerContext.getServer().runCommand("tp @a 0 " + (PLAYER_Y + 7) + " -10");
            singleplayerContext.getServer().runCommand("fill -9 " + PLAYER_Y + " -9 9 " + (PLAYER_Y + 6) + " 9 water");
            singleplayerContext.getServer().runCommand("give @a fishing_rod[enchantments={lure:4}]");
            waitClientbound(context, singleplayerContext);
            for (int i = 0; i < 2; i++) {
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                waitServerbound(context);
                waitFor(context, singleplayerContext.getServer(), server -> {
                    FishingHook hook = server.overworld().getEntities(EntityTypeTest.forClass(FishingHook.class), _ -> true).getFirst();
                    return hook.nibble > 0;
                }, 1000);
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
            waitServerbound(context);
            context.waitTicks(10);
        });
    }

    @TestGoal
    private static void testNeverWearChestplates(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Easy.NEVER_WEAR_CHESTPLATES, () -> {
            singleplayerContext.getServer().runCommand("give @a iron_chestplate");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testNeverUseShields(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Easy.NEVER_USE_SHIELDS, () -> {
            singleplayerContext.getServer().runCommand("give @a shield");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void test3x3x3GlassCube(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy._3X3X3_GLASS_CUBE, () -> {
            singleplayerContext.getServer().runCommand("fill 0 " + PLAYER_Y + " 1 2 " + (PLAYER_Y + 2) + " 3 glass");
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 1 air");
            singleplayerContext.getServer().runCommand("setblock 1 " + (PLAYER_Y + 1) + " 2 lava");
            singleplayerContext.getServer().runCommand("give @a glass");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testCreateSnowGolem(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.CREATE_SNOW_GOLEM, () -> {
            singleplayerContext.getServer().runCommand("fill 0 " + PLAYER_Y + " 1 0 " + (PLAYER_Y + 1) + " 1 snow_block");
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 0 stone");
            singleplayerContext.getServer().runCommand("give @a carved_pumpkin");
            singleplayerContext.getServer().runCommand("tp @a 0 " + (PLAYER_Y + 1) + " 0 0 30");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testFullIronArmor(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FULL_IRON_ARMOR, () -> {
            singleplayerContext.getServer().runCommand("give @a iron_helmet");
            singleplayerContext.getServer().runCommand("give @a iron_chestplate");
            singleplayerContext.getServer().runCommand("give @a iron_leggings");
            singleplayerContext.getServer().runCommand("give @a iron_boots");
        });
    }

    @TestGoal
    private static void testFillWaterCauldron(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FILL_WATER_CAULDRON, () -> {
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 1 cauldron");
            singleplayerContext.getServer().runCommand("give @a water_bucket");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testCompleteMap(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.COMPLETE_MAP, () -> {
            singleplayerContext.getServer().runCommand("give @a map");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    singleplayerContext.getServer().runCommand("tp @a " + (x * 128) + " " + PLAYER_Y + " " + (z * 128));
                    context.waitTicks(16);
                }
            }
        });
    }

    @TestGoal
    private static void testCompleteMapNoFill(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.COMPLETE_MAP, false, () -> {
            singleplayerContext.getServer().runCommand("give @a map");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal(dimension = TestDimension.THE_NETHER)
    private static void testSleepInNether(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.SLEEP_IN_NETHER, () -> {
            try {
                singleplayerContext.getServer().runCommand("execute in the_nether run setblock 0 128 1 obsidian");
                singleplayerContext.getServer().runCommand("execute in the_nether run setblock 0 128 2 white_bed[part=head]");
                singleplayerContext.getServer().runCommand("execute in the_nether run setblock 0 128 3 white_bed[part=foot]");
                singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 20");
                waitClientbound(context, singleplayerContext);
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                waitServerbound(context);
            } finally {
                singleplayerContext.getServer().runOnServer(server -> getServerPlayer(server).setHealth(20));
            }
        });
    }

    @TestGoal
    private static void test4x4Paintings(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy._4X4_PAINTINGS, () -> {
            singleplayerContext.getServer().runCommand("fill -10 " + PLAYER_Y + " 1 1 " + (PLAYER_Y + 3) + " 1 dirt");
            singleplayerContext.getServer().runCommand("give @a painting 3");
            Set<Holder<PaintingVariant>> seenPaintingVariants = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                singleplayerContext.getServer().runCommand("tp @a " + (i * -4) + " " + PLAYER_Y + " 0");
                for (int j = 0; j < 64; j++) {
                    waitClientbound(context, singleplayerContext);
                    context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                    waitServerbound(context);
                    int i_f = i;
                    boolean shouldContinue = singleplayerContext.getServer().computeOnServer(server -> {
                        Painting painting = server.overworld().getEntities(EntityTypeTest.forClass(Painting.class), new AABB(new BlockPos(i_f * -4, PLAYER_Y, 0)), _ -> true).getFirst();
                        if (seenPaintingVariants.add(painting.getVariant())) {
                            return true;
                        }
                        painting.remove(Entity.RemovalReason.DISCARDED);
                        return false;
                    });
                    if (shouldContinue) {
                        break;
                    }
                    singleplayerContext.getServer().runCommand("give @a painting");
                }
            }
        });
    }

    @TestGoal
    private static void testDoubleCreeperBoat(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.DOUBLE_CREEPER_BOAT, () -> {
            singleplayerContext.getServer().runCommand("summon oak_boat 0 " + PLAYER_Y + " 2");
            singleplayerContext.getServer().runCommand("summon creeper 0 " + PLAYER_Y + " 2");
            singleplayerContext.getServer().runCommand("ride @e[type=creeper,limit=1] mount @e[type=oak_boat,limit=1]");
            singleplayerContext.getServer().runCommand("summon creeper 0 " + PLAYER_Y + " 1");
            singleplayerContext.getServer().runCommand("ride @e[type=creeper,sort=nearest,limit=1] mount @e[type=oak_boat,limit=1]");
            singleplayerContext.getServer().runCommand("give @a diamond_sword");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 20 45");
            context.waitTicks(Math.max(EntityType.OAK_BOAT.updateInterval(), EntityType.CREEPER.updateInterval()));
            waitClientbound(context, singleplayerContext);
            context.waitTicks(10);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testVillagerTrade(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.VILLAGER_TRADE, () -> {
            singleplayerContext.getServer().runCommand("fill -1 " + PLAYER_Y + " 1 1 " + PLAYER_Y + " 3 oak_fence");
            singleplayerContext.getServer().runCommand("fill -1 " + (PLAYER_Y + 1) + " 3 1 " + (PLAYER_Y + 1) + " 3 oak_fence");
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 2 air");
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 3 fletching_table");
            singleplayerContext.getServer().runCommand("summon villager 0 " + PLAYER_Y + " 2");
            waitClientbound(context, singleplayerContext);
            context.waitFor(client ->
                Objects.requireNonNull(client.level)
                    .getEntities(EntityTypeTest.forClass(Villager.class), new AABB(new BlockPos(0, PLAYER_Y, 2)), _ -> true)
                    .getFirst()
                    .getVillagerData()
                    .profession()
                    .is(VillagerProfession.FLETCHER)
            );
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
            waitClientbound(context, singleplayerContext);
            List<ItemStack> itemCosts = context.computeOnClient(client -> {
                MerchantMenu menu = (MerchantMenu) Objects.requireNonNull(client.player).containerMenu;
                MerchantOffer offer = menu.getOffers().getFirst();
                ItemStack costB = offer.getCostB();
                return costB.isEmpty() ? List.of(offer.getCostA()) : List.of(offer.getCostA(), costB);
            });
            singleplayerContext.getServer().runOnServer(server -> {
                ServerPlayer player = getServerPlayer(server);
                for (ItemStack itemCost : itemCosts) {
                    player.getInventory().add(itemCost.copy());
                }
            });
            waitClientbound(context, singleplayerContext);
            context.runOnClient(client -> {
                var button = (MerchantScreen.TradeOfferButton) Screens.getWidgets(Objects.requireNonNull(client.screen))
                    .stream()
                    .filter(widget -> widget instanceof MerchantScreen.TradeOfferButton)
                    .findFirst()
                    .orElseThrow();
                button.onPress(new MouseButtonInfo(GLFW.GLFW_KEY_UNKNOWN, 0));
                AbstractContainerMenu menu = Objects.requireNonNull(client.player).containerMenu;
                Slot resultSlot = menu.slots.stream().filter(slot -> slot instanceof MerchantResultSlot).findFirst().orElseThrow();
                Objects.requireNonNull(client.gameMode).handleContainerInput(menu.containerId, resultSlot.index, InputConstants.MOUSE_BUTTON_LEFT, ContainerInput.QUICK_MOVE, client.player);
            });
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testDifferentColoredShields(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.DIFFERENT_COLORED_SHIELDS, () -> {
            singleplayerContext.getServer().runOnServer(server -> {
                ServerPlayer player = getServerPlayer(server);
                player.getInventory().add(makeShield(DyeColor.RED));
                player.getInventory().add(makeShield(DyeColor.GREEN));
                player.getInventory().add(makeShield(DyeColor.BLUE));
            });
        });
    }

    private static ItemStack makeShield(DyeColor color) {
        ItemStack shield = new ItemStack(Items.SHIELD);
        shield.set(DataComponents.BASE_COLOR, color);
        return shield;
    }

    @TestGoal
    private static void testGrowHugeMushroom(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.GROW_HUGE_MUSHROOM, () -> {
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 1 mycelium");
            singleplayerContext.getServer().runCommand("setblock 0 " + (PLAYER_Y + 1) + " 1 brown_mushroom");
            singleplayerContext.getServer().runCommand("give @a bone_meal 64");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 20");
            waitClientbound(context, singleplayerContext);
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            try {
                waitFor(context, singleplayerContext.getServer(), server -> !server.overworld().getBlockState(new BlockPos(0, PLAYER_Y + 1, 1)).is(Blocks.BROWN_MUSHROOM));
            } finally {
                context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
        });
    }

    @TestGoal
    private static void testBedRow(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.BED_ROW, () -> {
            for (int i = 0; i < 5; i++) {
                DyeColor color = DyeColor.VALUES.get(i);
                singleplayerContext.getServer().runCommand("setblock " + (i + 1) + " " + PLAYER_Y + " 1 " + color.getName() + "_bed[part=head]");
                singleplayerContext.getServer().runCommand("setblock " + (i + 1) + " " + PLAYER_Y + " 2 " + color.getName() + "_bed[part=foot]");
            }
            singleplayerContext.getServer().runCommand("give @a " + DyeColor.VALUES.get(5).getName() + "_bed");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 70");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testFinishAtSpawn(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FINISH_AT_SPAWN, () -> {
            singleplayerContext.getServer().runCommand("setworldspawn 1000 " + PLAYER_Y + " 0");
            singleplayerContext.getServer().runCommand("give @a compass");
            singleplayerContext.getServer().runCommand("tp @a 1000 " + PLAYER_Y + " 0");
            context.waitTicks(20);
        });
    }

    @TestGoal
    private static void testFinishAtSpawnNoCompass(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FINISH_AT_SPAWN, false, () -> {
            singleplayerContext.getServer().runCommand("setworldspawn 1000 " + PLAYER_Y + " 0");
            singleplayerContext.getServer().runCommand("tp @a 1000 " + PLAYER_Y + " 0");
            context.waitTicks(20);
        });
    }

    @TestGoal
    private static void testKillPassiveMobsWithOnlyFire(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.KILL_PASSIVE_MOBS_WITH_ONLY_FIRE, () -> {
            singleplayerContext.getServer().runCommand("fill -1 " + PLAYER_Y + " 1 1 " + PLAYER_Y + " 3 glass");
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 2 lava");
            for (int i = 0; i < 8; i++) {
                singleplayerContext.getServer().runCommand("summon chicken 0 " + PLAYER_Y + " 2");
            }
            waitFor(context, singleplayerContext.getServer(), server -> server.overworld().getEntities(EntityType.CHICKEN, new AABB(0, PLAYER_Y, 2, 1, PLAYER_Y + 2, 3), _ -> true).isEmpty());
        });
    }

    @TestGoal
    private static void testKillPassiveMobsWithOnlyFireFailed(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.KILL_PASSIVE_MOBS_WITH_ONLY_FIRE, false, () -> {
            singleplayerContext.getServer().runCommand("fill -1 " + PLAYER_Y + " 1 1 " + (PLAYER_Y + 1) + " 3 glass");
            singleplayerContext.getServer().runCommand("fill 0 " + PLAYER_Y + " 2 0 " + (PLAYER_Y + 1) + " 2 air");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s 0 " + (PLAYER_Y + 2) + " 1 0 70");
            int requiredAmount = singleplayerContext.getServer().computeOnServer(server -> Objects.requireNonNull(((MinecraftServerExt) server).bingo$getGame()).getBoard().getGoals()[0].requiredCount());
            for (int i = 0; i < requiredAmount; i++) {
                singleplayerContext.getServer().runCommand("summon cow 0 " + PLAYER_Y + " 2");
            }
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
            waitServerbound(context);
            singleplayerContext.getServer().runCommand("setblock 0 " + PLAYER_Y + " 2 lava");
            waitFor(context, singleplayerContext.getServer(), server -> server.overworld().getEntities(EntityType.COW, new AABB(0, PLAYER_Y, 2, 1, PLAYER_Y + 2, 3), _ -> true).isEmpty());
        });
    }

    @TestGoal
    private static void testWearDifferentColoredArmor(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.WEAR_DIFFERENT_COLORED_ARMOR, () -> {
            singleplayerContext.getServer().runOnServer(server -> {
                ServerPlayer player = getServerPlayer(server);
                player.getInventory().setItem(EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_HELMET, 0xff0000));
                player.getInventory().setItem(EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_CHESTPLATE, 0x00ff00));
                player.getInventory().setItem(EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_LEGGINGS, 0x0000ff));
                player.getInventory().add(new ItemStack(Items.LEATHER_BOOTS));
            });
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testWearDifferentColoredArmorNotWearing(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.WEAR_DIFFERENT_COLORED_ARMOR, false, () -> {
            singleplayerContext.getServer().runOnServer(server -> {
                ServerPlayer player = getServerPlayer(server);
                player.getInventory().setItem(EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_HELMET, 0xff0000));
                player.getInventory().setItem(EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_CHESTPLATE, 0x00ff00));
                player.getInventory().setItem(EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_LEGGINGS, 0x0000ff));
                player.getInventory().add(new ItemStack(Items.LEATHER_BOOTS));
            });
        });
    }

    @TestGoal
    private static void testWearDifferentColoredArmorSameColor(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.WEAR_DIFFERENT_COLORED_ARMOR, false, () -> {
            singleplayerContext.getServer().runOnServer(server -> {
                ServerPlayer player = getServerPlayer(server);
                player.getInventory().setItem(EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_HELMET, 0xff0000));
                player.getInventory().setItem(EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_CHESTPLATE, 0xff0000));
                player.getInventory().setItem(EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_LEGGINGS, 0xff0000));
                player.getInventory().setItem(EquipmentSlot.FEET.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_BOOTS, 0xff0000));
            });
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    private static ItemStack makeColoredArmor(Item item, int rgb) {
        ItemStack armor = new ItemStack(item);
        armor.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
        return armor;
    }

    @TestGoal
    private static void testNeverUseBoat(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal( context, singleplayerContext, GoalIds.Easy.NEVER_USE_BOAT, () -> {
            singleplayerContext.getServer().runCommand("summon oak_boat 0 " + PLAYER_Y + " 1");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 45");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal(dimension = TestDimension.THE_NETHER)
    private static void testPlaceFishInNether(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.PLACE_FISH_IN_NETHER, () -> {
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 90");
            singleplayerContext.getServer().runCommand("give @a salmon_bucket");
            waitClientbound(context, singleplayerContext);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            waitServerbound(context);
        });
    }

    @TestGoal
    private static void testDrownZombie(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.DROWN_ZOMBIE, () -> {
            singleplayerContext.getServer().runCommand("fill -1 " + PLAYER_Y + " 1 1 " + (PLAYER_Y + 1) + " 3 glass");
            singleplayerContext.getServer().runCommand("fill 0 " + PLAYER_Y + " 2 0 " + (PLAYER_Y + 1) + " 2 water");
            singleplayerContext.getServer().runCommand("execute as @a at @s run tp @s 0 " + (PLAYER_Y + 2) + " 1");
            Zombie zombie = singleplayerContext.getServer().computeOnServer(server -> {
                Zombie z = new Zombie(server.overworld());
                z.snapTo(Vec3.atBottomCenterOf(new BlockPos(0, PLAYER_Y, 2)));
                server.overworld().addFreshEntity(z);
                return z;
            });
            waitFor(context, singleplayerContext.getServer(), _ -> zombie.getTarget() != null);
            singleplayerContext.getServer().runOnServer(_ -> zombie.setInWaterTime(599)); // elapse the timer till the conversion
            context.waitTick(); // needs a tick to start converting
            singleplayerContext.getServer().runOnServer(_ -> {
                if (!zombie.isUnderWaterConverting()) {
                    throw new IllegalStateException("Zombie is not under water converting");
                }
                zombie.setConversionTime(0); // set the conversion timer straight down to zero
            });
            context.waitTick(); // finish the conversion
        });
    }

    private static void testGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Identifier goalId, Runnable testRunner) {
        testGoal(context, singleplayerContext, goalId, true, testRunner);
    }

    private static void testGoal(
        ClientGameTestContext context,
        TestSingleplayerContext singleplayerContext,
        Identifier goalId,
        @Nullable Boolean expectGoalAchievement,
        Runnable testRunner
    ) {
        commonSetup(context, singleplayerContext);
        singleplayerContext.getServer().runCommand("bingo start --require-goal " + goalId + " --size 1 red");
        context.waitTick();

        boolean testFailed = false;
        try {
            testRunner.run();
        } catch (Throwable e) {
            testFailed = true;
            LOGGER.error("Test failed", e);
        }

        context.waitTick();
        if (expectGoalAchievement != null) {
            testFailed |= singleplayerContext.getServer().computeOnServer(server -> (((MinecraftServerExt) server).bingo$getGame() == null) != expectGoalAchievement);
        }

        reportTestResult(context, goalId, testFailed);
    }

    private static void testNeverGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Identifier goalId, Runnable testRunner) {
        commonSetup(context, singleplayerContext);
        singleplayerContext.getServer().runCommand("bingo start --require-goal " + goalId + " --size 2 red");
        context.waitTick();

        boolean testFailed = false;
        try {
            testRunner.run();
        } catch (Throwable e) {
            testFailed = true;
            LOGGER.error("Test failed", e);
        }

        context.waitTick();
        testFailed |= singleplayerContext.getServer().computeOnServer(server -> {
            BingoGame game = Objects.requireNonNull(((MinecraftServerExt) server).bingo$getGame());
            return Arrays.stream(game.getBoard().getStates()).anyMatch(BingoBoard.Teams::any);
        });

        reportTestResult(context, goalId, testFailed);
    }

    private static void commonSetup(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        TestGoal testAnnotation = getCallingTest().getAnnotation(TestGoal.class);
        TestDimension dimension = testAnnotation.dimension();
        String dimensionName = dimension.name().toLowerCase(Locale.ROOT);

        context.runOnClient(client -> Objects.requireNonNull(client.player).setDeltaMovement(Vec3.ZERO));
        singleplayerContext.getServer().runCommand("execute in " + dimensionName + " run tp @a " + dimension.spawnPos.getX() + " " + dimension.spawnPos.getY() + " " + dimension.spawnPos.getZ() + " 0 0");
        waitClientbound(context, singleplayerContext);
        singleplayerContext.getClientLevel().waitForChunksDownload();
        singleplayerContext.getServer().runCommand("clear @a");
        singleplayerContext.getServer().runCommand("execute in " + dimensionName + " run fill " + (dimension.spawnPos.getX() - 16) + " " + dimension.spawnPos.getY() + " " + (dimension.spawnPos.getZ() - 16) + " " + (dimension.spawnPos.getX() + 16) + " " + (dimension.spawnPos.getY() + 16) + " " + (dimension.spawnPos.getZ() + 16) + " air");
        singleplayerContext.getServer().runOnServer(server -> {
            for (Entity entity : Objects.requireNonNull(server.getLevel(dimension.key)).getAllEntities()) {
                if (!(entity instanceof Player)) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        });
        waitClientbound(context, singleplayerContext);
    }

    private static void reportTestResult(ClientGameTestContext context, Identifier goalId, boolean testFailed) {
        String testName = getCallingTest().getName() + "(" + goalId + ")";

        if (testFailed) {
            failedTests.add(testName);
            context.runOnClient(client -> client.gui.getChat().addClientSystemMessage(Component.literal("Test failed: " + testName).withStyle(ChatFormatting.RED)));
        } else {
            context.runOnClient(client -> client.gui.getChat().addClientSystemMessage(Component.literal("Test passed: " + testName).withStyle(ChatFormatting.GREEN)));
        }
    }

    private static void waitClientbound(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        singleplayerContext.getServer().runOnServer(server -> server.getPlayerList().broadcastAll(ServerPlayNetworking.createClientboundPacket(GametestSyncPayload.INSTANCE)));
        context.waitFor(_ -> syncPacketReceived);
        syncPacketReceived = false;
    }

    private static void waitServerbound(ClientGameTestContext context) {
        context.runOnClient(_ -> ClientPlayNetworking.send(GametestSyncPayload.INSTANCE));
        context.waitFor(_ -> syncPacketReceived);
        syncPacketReceived = false;
    }

    private static <E extends Throwable> void waitFor(ClientGameTestContext context, TestServerContext serverContext, FailablePredicate<MinecraftServer, E> predicate) throws E {
        waitFor(context, serverContext, predicate, ClientGameTestContext.DEFAULT_TIMEOUT);
    }

    private static <E extends Throwable> void waitFor(ClientGameTestContext context, TestServerContext serverContext, FailablePredicate<MinecraftServer, E> predicate, int timeout) throws E {
        for (int i = 0; i < timeout; i++) {
            if (serverContext.computeOnServer(predicate::test)) {
                return;
            }
            context.waitTick();
        }

        if (!serverContext.computeOnServer(predicate::test)) {
            throw new AssertionError("Timed out waiting for predicate");
        }
    }

    private static ServerPlayer getServerPlayer(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    private static GoalProgress getGoalProgress(MinecraftServer server) {
        BingoGame game = Objects.requireNonNull(((MinecraftServerExt) server).bingo$getGame());
        return Objects.requireNonNull(game.getGoalProgress(getServerPlayer(server), game.getBoard().getGoals()[0]));
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(_ -> {
            PayloadTypeRegistry.serverboundPlay().register(GametestSyncPayload.TYPE, GametestSyncPayload.CODEC);
            PayloadTypeRegistry.clientboundPlay().register(GametestSyncPayload.TYPE, GametestSyncPayload.CODEC);
            ClientPlayNetworking.registerGlobalReceiver(GametestSyncPayload.TYPE, (_, _) -> syncPacketReceived = true);
            ServerPlayNetworking.registerGlobalReceiver(GametestSyncPayload.TYPE, (_, _) -> syncPacketReceived = true);
        });

        Consumer<WorldCreationUiState> settingsAdjustor = settings -> settings.getGameRules().set(GameRules.RANDOM_TICK_SPEED, 0, null);
        try (TestSingleplayerContext singleplayerContext = context.worldBuilder().adjustSettings(settingsAdjustor).create()) {
            singleplayerContext.getServer().runCommand("bingo teams create red");
            singleplayerContext.getServer().runCommand("bingo teams randomize");

            failedTests.clear();

            Map<TestDimension, List<Method>> testMethods = new EnumMap<>(TestDimension.class);
            boolean seenOnlyMe = false;

            for (Method method : getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(TestGoal.class)) {
                    if (!Modifier.isStatic(method.getModifiers())) {
                        throw new IllegalStateException("Goal test methods must be static");
                    }

                    TestGoal testGoal = method.getAnnotation(TestGoal.class);

                    if (seenOnlyMe) {
                        if (!testGoal.onlyMe()) {
                            continue;
                        }
                    } else if (testGoal.onlyMe()) {
                        seenOnlyMe = true;
                        testMethods.clear();
                    }

                    testMethods.computeIfAbsent(testGoal.dimension(), _ -> new ArrayList<>()).add(method);
                }
            }

            for (List<Method> methods : testMethods.values()) {
                for (Method method : methods) {
                    Object[] args = new Object[method.getParameterCount()];
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        Class<?> paramType = parameterTypes[i];
                        if (paramType == ClientGameTestContext.class) {
                            args[i] = context;
                        } else if (paramType == TestSingleplayerContext.class) {
                            args[i] = singleplayerContext;
                        } else {
                            throw new IllegalStateException("Illegal parameter type " + paramType + " in " + method);
                        }
                    }
                    try {
                        method.invoke(null, args);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw BingoUtil.sneakyThrow(e.getCause());
                    }
                }
            }
        }

        if (!failedTests.isEmpty()) {
            throw new IllegalStateException("There were failing tests: " + failedTests);
        }
    }

    private static Method getCallingTest() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames ->
            frames.map(frame -> {
                Class<?> clazz = frame.getDeclaringClass();
                try {
                    return clazz.getDeclaredMethod(frame.getMethodName(), frame.getMethodType().parameterArray());
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }).filter(method -> method.isAnnotationPresent(TestGoal.class)).findFirst().orElseThrow()
        );
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestGoal {
        boolean onlyMe() default false;
        TestDimension dimension() default TestDimension.OVERWORLD;
    }

    private enum TestDimension {
        OVERWORLD(Level.OVERWORLD, 0, PLAYER_Y, 0),
        THE_NETHER(Level.NETHER, 0, 128, 0),
        THE_END(Level.END, 100, 50, 0);

        final ResourceKey<Level> key;
        final BlockPos spawnPos;

        TestDimension(ResourceKey<Level> key, int spawnX, int spawnY, int spawnZ) {
            this.key = key;
            this.spawnPos = new BlockPos(spawnX, spawnY, spawnZ);
        }
    }
}
