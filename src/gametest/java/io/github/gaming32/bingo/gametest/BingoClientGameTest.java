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
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ConduitBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.ShelfMushroomBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.PotentSulfurState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

// Player is at 0, 0, facing south (towards +Z)
@SuppressWarnings("UnstableApiUsage")
public class BingoClientGameTest implements FabricClientGameTest {
    private static final Logger LOGGER = LogUtils.getLogger();
    // The superflat ground is above sea level, for goals that need to be below sea level
    private static final int PLAYER_Y = 65;
    private static final int GRASS_Y = PLAYER_Y - 1;
    private static final int DIRT_DEPTH = 2;
    private static final int BEDROCK_Y = -64;
    private static final List<String> failedTests = new ArrayList<>();

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

    @TestGoal(dimension = TestDimension.THE_NETHER)
    private static void testGrowTreeInNether(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.GROW_TREE_IN_NETHER, () -> {
            setBlock(singleplayerContext, new BlockPos(0, 128, 1), Blocks.DIRT);
            setBlock(singleplayerContext, new BlockPos(0, 129, 1), Blocks.OAK_SAPLING);
            give(singleplayerContext, Items.BONE_MEAL, 64);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            try {
                singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, 129, 1)).is(Blocks.OAK_LOG));
            } finally {
                context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
        });
    }

    @TestGoal
    private static void testShootButton(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.SHOOT_BUTTON, () -> {
            give(singleplayerContext, Items.BOW);
            give(singleplayerContext, Items.ARROW);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), Blocks.OAK_BUTTON.defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.FLOOR));
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            try {
                context.waitTicks(10);
                rotate(singleplayerContext, 0, 90);
                singleplayerContext.getConnection().waitForClientboundPackets();
                context.waitTicks(10);
            } finally {
                context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
            singleplayerContext.getConnection().waitForServerboundPackets();
            context.waitTicks(10);
        });
    }

    @TestGoal
    private static void testEatEntireCake(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.EAT_ENTIRE_CAKE, () -> {
            rotate(singleplayerContext, -30, 15);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.CAKE);
            addEffect(singleplayerContext, MobEffects.HUNGER, 64);
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            try {
                singleplayerContext.getServer().waitFor(
                    _ -> singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, PLAYER_Y + 1, 1)).isAir(),
                    15 * SharedConstants.TICKS_PER_SECOND
                );
            } finally {
                context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
        });
    }

    @TestGoal
    private static void testFishTreasureAndJunk(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FISH_TREASURE_JUNK, () -> {
            // seed 16 gets a treasure and a junk item on the first two fishes
            resetRandomSequence(singleplayerContext, BuiltInLootTables.FISHING.identifier(), 16, false);
            fill(singleplayerContext, new BlockPos(-10, PLAYER_Y, -10), new BlockPos(10, PLAYER_Y + 6, 10), Blocks.GLASS);
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 7, -10));
            fill(singleplayerContext, new BlockPos(-9, PLAYER_Y, -9), new BlockPos(9, PLAYER_Y + 6, 9), Blocks.WATER);
            give(singleplayerContext, enchantedItem(singleplayerContext, Items.FISHING_ROD, Enchantments.LURE, 4));
            singleplayerContext.getConnection().waitForClientboundPackets();
            for (int i = 0; i < 2; i++) {
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                singleplayerContext.getConnection().waitForServerboundPackets();
                singleplayerContext.getServer().waitFor(_ -> {
                    FishingHook hook = singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypeTest.forClass(FishingHook.class), _ -> true).getFirst();
                    return hook.nibble > 0;
                }, 1000);
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
            singleplayerContext.getConnection().waitForServerboundPackets();
            context.waitTicks(10);
        });
    }

    @TestGoal
    private static void testNeverWearChestplates(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Easy.NEVER_WEAR_CHESTPLATES, () -> {
            give(singleplayerContext, Items.IRON_CHESTPLATE);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testNeverUseShields(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testNeverGoal(context, singleplayerContext, GoalIds.Easy.NEVER_USE_SHIELDS, () -> {
            give(singleplayerContext, Items.SHIELD);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void test3x3x3GlassCube(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy._3X3X3_GLASS_CUBE, () -> {
            fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), new BlockPos(2, PLAYER_Y + 2, 3), Blocks.GLASS);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.AIR);
            setBlock(singleplayerContext, new BlockPos(1, PLAYER_Y + 1, 2), Blocks.LAVA);
            give(singleplayerContext, Items.GLASS);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testCreateSnowGolem(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.CREATE_SNOW_GOLEM, () -> {
            fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), new BlockPos(0, PLAYER_Y + 1, 1), Blocks.SNOW_BLOCK);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), Blocks.STONE);
            give(singleplayerContext, Items.CARVED_PUMPKIN);
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 0), 0, 30);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testFullIronArmor(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FULL_IRON_ARMOR, () -> {
            give(singleplayerContext, Items.IRON_HELMET);
            give(singleplayerContext, Items.IRON_CHESTPLATE);
            give(singleplayerContext, Items.IRON_LEGGINGS);
            give(singleplayerContext, Items.IRON_BOOTS);
        });
    }

    @TestGoal
    private static void testFillWaterCauldron(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FILL_WATER_CAULDRON, () -> {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.CAULDRON);
            give(singleplayerContext, Items.WATER_BUCKET);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testCompleteMap(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.COMPLETE_MAP, () -> {
            give(singleplayerContext, Items.MAP);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    teleport(singleplayerContext, new BlockPos(x * 128, PLAYER_Y, z * 128));
                    context.waitTicks(16);
                }
            }
        });
    }

    @TestGoal
    private static void testCompleteMapNoFill(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.COMPLETE_MAP, false, () -> {
            give(singleplayerContext, Items.MAP);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal(dimension = TestDimension.THE_NETHER)
    private static void testSleepInNether(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.SLEEP_IN_NETHER, () -> {
            setBlock(singleplayerContext, new BlockPos(0, 128, 1), Blocks.OBSIDIAN);
            placeBed(singleplayerContext, new BlockPos(0, 128, 2), Blocks.BED.white());
            rotate(singleplayerContext, 0, 20);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void test4x4Paintings(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy._4X4_PAINTINGS, () -> {
            fill(singleplayerContext, new BlockPos(-10, PLAYER_Y, 1), new BlockPos(1, PLAYER_Y + 3, 1), Blocks.DIRT);
            give(singleplayerContext, Items.PAINTING, 3);
            Set<Holder<PaintingVariant>> seenPaintingVariants = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                teleport(singleplayerContext, new BlockPos(i * -4, PLAYER_Y, 0));
                for (int j = 0; j < 64; j++) {
                    singleplayerContext.getConnection().waitForClientboundPackets();
                    context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                    singleplayerContext.getConnection().waitForServerboundPackets();
                    int i_f = i;
                    boolean shouldContinue = singleplayerContext.getServer().computeOnServer(_ -> {
                        Painting painting = singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypeTest.forClass(Painting.class), new AABB(new BlockPos(i_f * -4, PLAYER_Y, 0)), _ -> true).getFirst();
                        if (seenPaintingVariants.add(painting.getVariant())) {
                            return true;
                        }
                        painting.remove(Entity.RemovalReason.DISCARDED);
                        return false;
                    });
                    if (shouldContinue) {
                        break;
                    }
                    give(singleplayerContext, Items.PAINTING);
                }
            }
        });
    }

    @TestGoal
    private static void testDoubleCreeperBoat(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.DOUBLE_CREEPER_BOAT, () -> {
            Boat boat = summon(singleplayerContext, EntityTypes.OAK_BOAT, new BlockPos(0, PLAYER_Y, 2));
            ride(singleplayerContext, summon(singleplayerContext, EntityTypes.CREEPER, new BlockPos(0, PLAYER_Y, 2)), boat);
            ride(singleplayerContext, summon(singleplayerContext, EntityTypes.CREEPER, new BlockPos(0, PLAYER_Y, 1)), boat);
            give(singleplayerContext, Items.DIAMOND_SWORD);
            rotate(singleplayerContext, 20, 45);
            context.waitTicks(Math.max(EntityTypes.OAK_BOAT.updateInterval(), EntityTypes.CREEPER.updateInterval()));
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.waitTicks(10);
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testVillagerTrade(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.VILLAGER_TRADE, () -> {
            fill(singleplayerContext, new BlockPos(-1, PLAYER_Y, 1), new BlockPos(1, PLAYER_Y, 3), Blocks.OAK_FENCE);
            fill(singleplayerContext, new BlockPos(-1, PLAYER_Y + 1, 3), new BlockPos(1, PLAYER_Y + 1, 3), Blocks.OAK_FENCE);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 2), Blocks.AIR);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 3), Blocks.FLETCHING_TABLE);
            summon(singleplayerContext, EntityTypes.VILLAGER, new BlockPos(0, PLAYER_Y, 2));
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.waitFor(client ->
                Objects.requireNonNull(client.level)
                    .getEntities(EntityTypeTest.forClass(Villager.class), new AABB(new BlockPos(0, PLAYER_Y, 2)), _ -> true)
                    .getFirst()
                    .getVillagerData()
                    .profession()
                    .is(VillagerProfession.FLETCHER)
            );
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            singleplayerContext.getConnection().waitForClientboundPackets();
            List<ItemStack> itemCosts = context.computeOnClient(client -> {
                MerchantMenu menu = (MerchantMenu) Objects.requireNonNull(client.player).containerMenu;
                MerchantOffer offer = menu.getOffers().getFirst();
                ItemStack costB = offer.getCostB();
                return costB.isEmpty() ? List.of(offer.getCostA()) : List.of(offer.getCostA(), costB);
            });
            for (ItemStack itemCost : itemCosts) {
                give(singleplayerContext, itemCost);
            }
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.runOnClient(client -> {
                var button = (MerchantScreen.TradeOfferButton) Screens.getWidgets(Objects.requireNonNull(client.gui.screen()))
                    .stream()
                    .filter(widget -> widget instanceof MerchantScreen.TradeOfferButton)
                    .findFirst()
                    .orElseThrow();
                button.onPress(new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
                AbstractContainerMenu menu = Objects.requireNonNull(client.player).containerMenu;
                Slot resultSlot = menu.slots.stream().filter(slot -> slot instanceof MerchantResultSlot).findFirst().orElseThrow();
                Objects.requireNonNull(client.gameMode).handleContainerInput(menu.containerId, resultSlot.index, InputConstants.MOUSE_BUTTON_LEFT, ContainerInput.QUICK_MOVE, client.player);
            });
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testDifferentColoredShields(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.DIFFERENT_COLORED_SHIELDS, () -> {
            give(singleplayerContext, makeShield(DyeColor.RED));
            give(singleplayerContext, makeShield(DyeColor.GREEN));
            give(singleplayerContext, makeShield(DyeColor.BLUE));
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
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 1), Blocks.MYCELIUM);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.BROWN_MUSHROOM);
            give(singleplayerContext, Items.BONE_MEAL, 64);
            rotate(singleplayerContext, 0, 20);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            try {
                singleplayerContext.getServer().waitFor(_ -> !singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, PLAYER_Y + 1, 1)).is(Blocks.BROWN_MUSHROOM));
            } finally {
                context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            }
        });
    }

    @TestGoal
    private static void testBedRow(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.BED_ROW, () -> {
            for (int i = 0; i < 5; i++) {
                placeBed(singleplayerContext, new BlockPos(i + 1, PLAYER_Y, 1), Blocks.BED.pick(DyeColor.VALUES.get(i)));
            }
            give(singleplayerContext, Items.BED.pick(DyeColor.VALUES.get(5)));
            rotate(singleplayerContext, 0, 70);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testFinishAtSpawn(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FINISH_AT_SPAWN, () -> {
            setWorldSpawn(singleplayerContext, new BlockPos(1000, PLAYER_Y, 0));
            give(singleplayerContext, Items.COMPASS);
            teleport(singleplayerContext, new BlockPos(1000, PLAYER_Y, 0));
            context.waitTicks(20);
        });
    }

    @TestGoal
    private static void testFinishAtSpawnNoCompass(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FINISH_AT_SPAWN, false, () -> {
            setWorldSpawn(singleplayerContext, new BlockPos(1000, PLAYER_Y, 0));
            teleport(singleplayerContext, new BlockPos(1000, PLAYER_Y, 0));
            context.waitTicks(20);
        });
    }

    @TestGoal
    private static void testKillPassiveMobsWithOnlyFire(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.KILL_PASSIVE_MOBS_WITH_ONLY_FIRE, () -> {
            fill(singleplayerContext, new BlockPos(-1, PLAYER_Y, 1), new BlockPos(1, PLAYER_Y, 3), Blocks.GLASS);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 2), Blocks.LAVA);
            for (int i = 0; i < 8; i++) {
                summon(singleplayerContext, EntityTypes.CHICKEN, new BlockPos(0, PLAYER_Y, 2));
            }
            singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypes.CHICKEN, new AABB(0, PLAYER_Y, 2, 1, PLAYER_Y + 2, 3), _ -> true).isEmpty());
        });
    }

    @TestGoal
    private static void testKillPassiveMobsWithOnlyFireFailed(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.KILL_PASSIVE_MOBS_WITH_ONLY_FIRE, false, () -> {
            fill(singleplayerContext, new BlockPos(-1, PLAYER_Y, 1), new BlockPos(1, PLAYER_Y + 1, 3), Blocks.GLASS);
            fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 2), new BlockPos(0, PLAYER_Y + 1, 2), Blocks.AIR);
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 2, 1), 0, 70);
            int requiredAmount = singleplayerContext.getServer().computeOnServer(server -> Objects.requireNonNull(((MinecraftServerExt) server).bingo$getGame()).getBoard().getGoals()[0].requiredCount());
            for (int i = 0; i < requiredAmount; i++) {
                summon(singleplayerContext, EntityTypes.COW, new BlockPos(0, PLAYER_Y, 2));
            }
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 2), Blocks.LAVA);
            singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypes.COW, new AABB(0, PLAYER_Y, 2, 1, PLAYER_Y + 2, 3), _ -> true).isEmpty());
        });
    }

    @TestGoal
    private static void testWearDifferentColoredArmor(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.WEAR_DIFFERENT_COLORED_ARMOR, () -> {
            wearArmorThenEquipBoots(
                context, singleplayerContext,
                makeColoredArmor(Items.LEATHER_HELMET, 0xff0000),
                makeColoredArmor(Items.LEATHER_CHESTPLATE, 0x00ff00),
                makeColoredArmor(Items.LEATHER_LEGGINGS, 0x0000ff),
                new ItemStack(Items.LEATHER_BOOTS)
            );
        });
    }

    @TestGoal
    private static void testWearDifferentColoredArmorNotWearing(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.WEAR_DIFFERENT_COLORED_ARMOR, false, () -> {
            equip(singleplayerContext, EquipmentSlot.HEAD, makeColoredArmor(Items.LEATHER_HELMET, 0xff0000));
            equip(singleplayerContext, EquipmentSlot.CHEST, makeColoredArmor(Items.LEATHER_CHESTPLATE, 0x00ff00));
            equip(singleplayerContext, EquipmentSlot.LEGS, makeColoredArmor(Items.LEATHER_LEGGINGS, 0x0000ff));
            give(singleplayerContext, Items.LEATHER_BOOTS);
        });
    }

    @TestGoal
    private static void testWearDifferentColoredArmorSameColor(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.WEAR_DIFFERENT_COLORED_ARMOR, false, () -> {
            wearArmorThenEquipBoots(
                context, singleplayerContext,
                makeColoredArmor(Items.LEATHER_HELMET, 0xff0000),
                makeColoredArmor(Items.LEATHER_CHESTPLATE, 0xff0000),
                makeColoredArmor(Items.LEATHER_LEGGINGS, 0xff0000),
                makeColoredArmor(Items.LEATHER_BOOTS, 0xff0000)
            );
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
            summon(singleplayerContext, EntityTypes.OAK_BOAT, new BlockPos(0, PLAYER_Y, 1));
            rotate(singleplayerContext, 0, 45);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal(dimension = TestDimension.THE_NETHER)
    private static void testPlaceFishInNether(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.PLACE_FISH_IN_NETHER, () -> {
            rotate(singleplayerContext, 0, 90);
            give(singleplayerContext, Items.SALMON_BUCKET);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testDrownZombie(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.DROWN_ZOMBIE, () -> {
            fill(singleplayerContext, new BlockPos(-1, PLAYER_Y, 1), new BlockPos(1, PLAYER_Y + 1, 3), Blocks.GLASS);
            fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 2), new BlockPos(0, PLAYER_Y + 1, 2), Blocks.WATER);
            teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 2, 1));
            Zombie zombie = summon(singleplayerContext, EntityTypes.ZOMBIE, new BlockPos(0, PLAYER_Y, 2));
            singleplayerContext.getServer().waitFor(_ -> zombie.getTarget() != null);
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
    private static void testDifferentSlabs(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryEasy.DIFFERENT_SLABS, () -> {
            for (Item item : List.of(Items.WOOL_SLAB.white(), Items.OAK_SLAB, Items.SPRUCE_SLAB, Items.BIRCH_SLAB)) {
                give(singleplayerContext, item);
            }
        });
    }

    @TestGoal
    private static void testDifferentSlabsGroupsCountOnce(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.DIFFERENT_SLABS, false, () -> {
            // Only counts as 4 different slabs, as all the wool ones count as 1, and all the concrete ones count as 1
            for (DyeColor color : List.of(DyeColor.WHITE, DyeColor.RED, DyeColor.YELLOW, DyeColor.BLUE)) {
                give(singleplayerContext, Items.WOOL_SLAB.pick(color));
                give(singleplayerContext, Items.CONCRETE_SLAB.pick(color));
            }
            give(singleplayerContext, Items.OAK_SLAB);
            give(singleplayerContext, Items.SPRUCE_SLAB);
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

    @TestGoal
    private static void testDifferentStairsGroupsCountOnce(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.DIFFERENT_STAIRS, false, () -> {
            // Only counts as 4 different stairs, as all the wool ones count as 1, and all the concrete ones count as 1
            for (DyeColor color : List.of(DyeColor.WHITE, DyeColor.RED, DyeColor.YELLOW, DyeColor.BLUE)) {
                give(singleplayerContext, Items.WOOL_STAIRS.pick(color));
                give(singleplayerContext, Items.CONCRETE_STAIRS.pick(color));
            }
            give(singleplayerContext, Items.OAK_STAIRS);
            give(singleplayerContext, Items.SPRUCE_STAIRS);
        });
    }

    @TestGoal
    private static void testSitOnCushion(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.SIT_ON_CUSHION, () -> {
            summon(singleplayerContext, EntityTypes.CUSHION, new BlockPos(0, PLAYER_Y, 1));
            rotate(singleplayerContext, 0, 57);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testSleepInStrawBed(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.SLEEP_IN_STRAW_BED, () -> {
            setTime(singleplayerContext, ClockTimeMarkers.NIGHT);
            placeBed(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.STRAW_BED);
            rotate(singleplayerContext, 0, 15);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testSleepInStrawBedNormalBed(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.SLEEP_IN_STRAW_BED, false, () -> {
            setTime(singleplayerContext, ClockTimeMarkers.NIGHT);
            placeBed(singleplayerContext, new BlockPos(0, PLAYER_Y + 1, 1), Blocks.BED.white());
            rotate(singleplayerContext, 0, 15);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
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

    private static void growHugeFungus(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, BlockPos fungusPos, float xRot) {
        setBlock(singleplayerContext, fungusPos.below(), Blocks.CRIMSON_NYLIUM);
        setBlock(singleplayerContext, fungusPos, Blocks.CRIMSON_FUNGUS);
        give(singleplayerContext, Items.BONE_MEAL, 64);
        rotate(singleplayerContext, 0, xRot);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        try {
            singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(fungusPos).is(Blocks.CRIMSON_STEM));
        } finally {
            context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        }
        singleplayerContext.getConnection().waitForServerboundPackets();
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

    private static void wearArmorThenEquipBoots(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Item helmet, Item chestplate, Item leggings, Item boots) {
        wearArmorThenEquipBoots(context, singleplayerContext, new ItemStack(helmet), new ItemStack(chestplate), new ItemStack(leggings), new ItemStack(boots));
    }

    private static void wearArmorThenEquipBoots(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        equip(singleplayerContext, EquipmentSlot.HEAD, helmet);
        equip(singleplayerContext, EquipmentSlot.CHEST, chestplate);
        equip(singleplayerContext, EquipmentSlot.LEGS, leggings);
        give(singleplayerContext, boots);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
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

    // Gives the items split between two separate stacks
    private static void giveSplitStacks(TestSingleplayerContext singleplayerContext, int count, Item... items) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            Inventory inventory = player.getInventory();
            for (Item item : items) {
                inventory.setItem(inventory.getFreeSlot(), new ItemStack(item, count / 2));
                inventory.setItem(inventory.getFreeSlot(), new ItemStack(item, count - count / 2));
            }
            player.containerMenu.broadcastChanges();
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
    private static void testDifferentPotions(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.DIFFERENT_POTIONS, () -> {
            int count = getGoalNameIntArg(singleplayerContext, 0);
            for (Holder<Potion> potion : DIFFERENT_POTIONS.subList(0, count)) {
                give(singleplayerContext, PotionContents.createItemStack(Items.POTION, potion));
            }
        });
    }

    @TestGoal
    private static void testDifferentPotionsVariants(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        // "Splash, Lingering, Enhanced and Extended variants DON'T count!"
        testGoal(context, singleplayerContext, GoalIds.VeryHard.DIFFERENT_POTIONS, false, () -> {
            int count = getGoalNameIntArg(singleplayerContext, 0);
            List<Holder<Potion>> potions = DIFFERENT_POTIONS.subList(0, count - 1);
            if (!potions.contains(Potions.SWIFTNESS)) {
                throw new IllegalStateException("Swiftness should be in the list of potions");
            }
            for (Holder<Potion> potion : potions) {
                give(singleplayerContext, PotionContents.createItemStack(Items.POTION, potion));
            }
            give(singleplayerContext, PotionContents.createItemStack(Items.POTION, Potions.LONG_SWIFTNESS));
            give(singleplayerContext, PotionContents.createItemStack(Items.POTION, Potions.STRONG_SWIFTNESS));
            give(singleplayerContext, PotionContents.createItemStack(Items.SPLASH_POTION, Potions.SWIFTNESS));
            give(singleplayerContext, PotionContents.createItemStack(Items.LINGERING_POTION, Potions.SWIFTNESS));
        });
    }

    private static final List<Holder<Potion>> DIFFERENT_POTIONS = List.of(
        Potions.NIGHT_VISION, Potions.INVISIBILITY, Potions.LEAPING, Potions.FIRE_RESISTANCE, Potions.SWIFTNESS,
        Potions.SLOWNESS, Potions.TURTLE_MASTER, Potions.WATER_BREATHING, Potions.HEALING, Potions.HARMING,
        Potions.POISON, Potions.REGENERATION, Potions.STRENGTH, Potions.WEAKNESS, Potions.SLOW_FALLING,
        Potions.WIND_CHARGED, Potions.WEAVING, Potions.OOZING, Potions.INFESTED, Potions.LUCK
    );

    @TestGoal
    private static void testLevels(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.LEVELS, () -> {
            singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().giveExperienceLevels(50));
            context.waitTicks(2);
        });
    }

    @TestGoal
    private static void testFullPowerConduit(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.FULL_POWER_CONDUIT, () -> {
            BlockPos conduitPos = new BlockPos(0, PLAYER_Y + 3, 5);
            fill(singleplayerContext, conduitPos.offset(-3, -3, -3), conduitPos.offset(3, 3, 3), Blocks.GLASS);
            fill(singleplayerContext, conduitPos.offset(-2, -2, -2), conduitPos.offset(2, 2, 2), Blocks.WATER);
            // The full frame is a ring of prismarine in each of the three planes through the conduit
            for (BlockPos offset : BlockPos.betweenClosed(-2, -2, -2, 2, 2, 2)) {
                int x = Math.abs(offset.getX()), y = Math.abs(offset.getY()), z = Math.abs(offset.getZ());
                if (x == 0 && Math.max(y, z) == 2 || y == 0 && Math.max(x, z) == 2 || z == 0 && Math.max(x, y) == 2) {
                    setBlock(singleplayerContext, conduitPos.offset(offset), Blocks.PRISMARINE);
                }
            }
            setBlock(singleplayerContext, conduitPos, Blocks.CONDUIT.defaultBlockState().setValue(ConduitBlock.WATERLOGGED, true));
            waitForGameEnd(singleplayerContext, 200);
        });
    }

    @TestGoal
    private static void testZombifyPig(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.ZOMBIFY_PIG, () -> {
            summon(singleplayerContext, EntityTypes.PIG, new BlockPos(0, PLAYER_Y, 6));
            summon(singleplayerContext, EntityTypes.LIGHTNING_BOLT, new BlockPos(0, PLAYER_Y, 6));
            waitForGameEnd(singleplayerContext, 40);
        });
    }

    @TestGoal
    private static void testPartyParrots(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.PARTY_PARROTS, () -> {
            // Parrots in a glass cage behind the jukebox
            fill(singleplayerContext, new BlockPos(-2, PLAYER_Y, 2), new BlockPos(2, PLAYER_Y + 2, 5), Blocks.GLASS);
            fill(singleplayerContext, new BlockPos(-1, PLAYER_Y, 3), new BlockPos(1, PLAYER_Y + 1, 4), Blocks.AIR);
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y, 2), Blocks.JUKEBOX);
            Parrot.Variant[] variants = Parrot.Variant.values();
            for (int i = 0; i < variants.length; i++) {
                Parrot parrot = summon(singleplayerContext, EntityTypes.PARROT, new BlockPos(i % 3 - 1, PLAYER_Y, 3 + i / 3));
                Parrot.Variant variant = variants[i];
                singleplayerContext.getServer().runOnServer(_ -> parrot.setComponent(DataComponents.PARROT_VARIANT, variant));
            }
            give(singleplayerContext, Items.MUSIC_DISC_CAT);
            rotate(singleplayerContext, 0, 37);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
            context.waitTicks(5);
        });
    }

    @TestGoal
    private static void testKillEndermanWithEndermites(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.KILL_ENDERMAN_WITH_ENDERMITES, () -> {
            killEndermanWithEndermite(context, singleplayerContext, false);
        });
    }

    @TestGoal
    private static void testKillEndermanWithEndermitesHurtByPlayer(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.KILL_ENDERMAN_WITH_ENDERMITES, false, () -> {
            killEndermanWithEndermite(context, singleplayerContext, true);
        });
    }

    private static void killEndermanWithEndermite(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, boolean hurtByPlayer) {
        // Enclose them in stone so the enderman can't see the player or the sky, and make it night so the enderman doesn't teleport
        setTime(singleplayerContext, ClockTimeMarkers.NIGHT);
        fill(singleplayerContext, new BlockPos(-2, PLAYER_Y, 2), new BlockPos(2, PLAYER_Y + 4, 6), Blocks.STONE);
        fill(singleplayerContext, new BlockPos(-1, PLAYER_Y, 3), new BlockPos(1, PLAYER_Y + 3, 5), Blocks.AIR);
        Enderman enderman = summon(singleplayerContext, EntityTypes.ENDERMAN, new BlockPos(0, PLAYER_Y, 4));
        Endermite endermite = summon(singleplayerContext, EntityTypes.ENDERMITE, new BlockPos(1, PLAYER_Y, 4));
        if (hurtByPlayer) {
            damageByPlayer(singleplayerContext, enderman, 1);
        }
        singleplayerContext.getServer().runOnServer(_ -> {
            enderman.setHealth(1);
            endermite.setTarget(enderman);
        });
        singleplayerContext.getServer().waitFor(_ -> !enderman.isAlive(), 400);
        context.waitTicks(5);
    }

    @TestGoal
    private static void testBeaconRegen(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.BEACON_REGEN, () -> {
            BlockPos beaconPos = new BlockPos(0, PLAYER_Y + 4, 6);
            for (int level = 1; level <= 4; level++) {
                fill(singleplayerContext, beaconPos.offset(-level, -level, -level), beaconPos.offset(level, -level, level), Blocks.IRON_BLOCK);
            }
            setBlock(singleplayerContext, beaconPos, Blocks.BEACON);
            teleport(singleplayerContext, beaconPos.offset(0, 0, -1), 0, 60);
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            context.waitForScreen(BeaconScreen.class);
            // The beacon only checks its pyramid every 80 ticks
            context.waitFor(client -> ((BeaconMenu) Objects.requireNonNull(client.player).containerMenu).getLevels() == 4, 100);
            singleplayerContext.getServer().runOnServer(_ -> {
                BeaconMenu menu = (BeaconMenu) singleplayerContext.getConnection().getServerPlayer().containerMenu;
                menu.getSlot(0).set(new ItemStack(Items.IRON_INGOT));
                menu.broadcastChanges();
            });
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.runOnClient(client -> Objects.requireNonNull(client.getConnection()).send(new ServerboundSetBeaconPacket(Optional.of(MobEffects.SPEED), Optional.of(MobEffects.REGENERATION))));
            singleplayerContext.getConnection().waitForServerboundPackets();
            singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().closeContainer());
            waitForGameEnd(singleplayerContext, 300);
        });
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
    private static void testAllMineralBlocks(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.ALL_MINERAL_BLOCKS, () -> {
            buildMineralPillar(context, singleplayerContext, List.of(
                Blocks.COAL_BLOCK, Blocks.COPPER_BLOCK.weathering().unaffected(), Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK,
                Blocks.REDSTONE_BLOCK, Blocks.LAPIS_BLOCK, Blocks.EMERALD_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.NETHERITE_BLOCK
            ));
        });
    }

    // Places all but the last block directly, then the player places the last block on top
    private static void buildMineralPillar(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, List<Block> blocks) {
        int height = blocks.size() - 1;
        for (int i = 0; i < height; i++) {
            setBlock(singleplayerContext, new BlockPos(0, PLAYER_Y + i, 1), blocks.get(i));
        }
        teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + height, 0), 0, 60);
        fill(singleplayerContext, new BlockPos(0, PLAYER_Y, 0), new BlockPos(0, PLAYER_Y + height - 1, 0), Blocks.GLASS);
        give(singleplayerContext, blocks.getLast());
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        if (!singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerLevel().getBlockState(new BlockPos(0, PLAYER_Y + height, 1)).is(blocks.getLast()))) {
            throw new IllegalStateException("Top block was not placed");
        }
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

    private static void eat(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Item food) {
        singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().getFoodData().setFoodLevel(10));
        give(singleplayerContext, food);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        try {
            singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerPlayer().getInventory().getSelectedItem().isEmpty(), 100);
        } finally {
            context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        }
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
            testFailed |= isGameOver(singleplayerContext) != expectGoalAchievement;
        }

        reportTestResult(context, goalId, testFailed);
    }

    private static void testNeverGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Identifier goalId, Runnable testRunner) {
        testNeverGoal(context, singleplayerContext, goalId, true, testRunner);
    }

    private static void testNeverGoal(
        ClientGameTestContext context,
        TestSingleplayerContext singleplayerContext,
        Identifier goalId,
        boolean expectGoalFailure,
        Runnable testRunner
    ) {
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
            return Arrays.stream(game.getBoard().getStates()).anyMatch(BingoBoard.Teams::any) == expectGoalFailure;
        });

        reportTestResult(context, goalId, testFailed);
    }

    private static void commonSetup(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        TestGoal testAnnotation = getCallingTest().getAnnotation(TestGoal.class);
        TestDimension dimension = testAnnotation.dimension();
        // The player is invulnerable after respawning (e.g. from a previous test) until the client has loaded
        singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerPlayer().connection.hasClientLoaded());
        context.runOnClient(client -> Objects.requireNonNull(client.player).setDeltaMovement(Vec3.ZERO));
        singleplayerContext.getServer().runOnServer(server -> {
            ServerLevel level = Objects.requireNonNull(server.getLevel(dimension.key));
            teleport(level, singleplayerContext.getConnection().getServerPlayer(), Vec3.atBottomCenterOf(dimension.spawnPos), 0, 0);
        });
        singleplayerContext.getConnection().waitForClientboundPackets();
        singleplayerContext.getConnection().waitForChunksDownload();
        clearInventory(singleplayerContext);
        singleplayerContext.getServer().runOnServer(server -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            if (player.containerMenu != player.inventoryMenu) {
                player.closeContainer();
            }
            player.removeAllEffects();
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.setAirSupply(player.getMaxAirSupply());
            player.clearFire();
            player.setExperienceLevels(0);

            ServerLevel level = Objects.requireNonNull(server.getLevel(dimension.key));
            fill(level, dimension.spawnPos.offset(-16, 0, -16), dimension.spawnPos.offset(16, 16, 16), Blocks.AIR.defaultBlockState());
            if (dimension == TestDimension.OVERWORLD) {
                // Restore the top of the superflat layers, in case a test modified them
                restoreGround(level, new BlockPos(-16, PLAYER_Y - 8, -16), new BlockPos(16, GRASS_Y, 16));
                level.dimensionType().defaultClock().ifPresent(clock -> server.clockManager().moveToTimeMarker(clock, ClockTimeMarkers.DAY));
            }
            // Copy the entities first, since removing them while iterating breaks the iterator
            List<Entity> entities = new ArrayList<>();
            level.getAllEntities().forEach(entities::add);
            for (Entity entity : entities) {
                if (!(entity instanceof Player)) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        });
        singleplayerContext.getConnection().waitForClientboundPackets();
    }

    // Restores the superflat layers in the given area
    private static void restoreGround(ServerLevel level, BlockPos from, BlockPos to) {
        int minY = Math.min(from.getY(), to.getY());
        int maxY = Math.max(from.getY(), to.getY());
        for (int y = minY; y <= maxY; y++) {
            BlockState state;
            if (y == BEDROCK_Y) {
                state = Blocks.BEDROCK.defaultBlockState();
            } else if (y < GRASS_Y - DIRT_DEPTH) {
                state = Blocks.STONE.defaultBlockState();
            } else if (y < GRASS_Y) {
                state = Blocks.DIRT.defaultBlockState();
            } else if (y == GRASS_Y) {
                state = Blocks.GRASS_BLOCK.defaultBlockState();
            } else {
                state = Blocks.AIR.defaultBlockState();
            }
            fill(level, from.atY(y), to.atY(y), state);
        }
    }

    private static void restoreGround(TestSingleplayerContext singleplayerContext, BlockPos from, BlockPos to) {
        singleplayerContext.getServer().runOnServer(_ -> restoreGround(singleplayerContext.getConnection().getServerLevel(), from, to));
    }

    private static void reportTestResult(ClientGameTestContext context, Identifier goalId, boolean testFailed) {
        String testName = getCallingTest().getName() + "(" + goalId + ")";

        if (testFailed) {
            failedTests.add(testName);
            context.runOnClient(client -> client.gui.hud.getChat().addClientSystemMessage(Component.literal("Test failed: " + testName).withStyle(ChatFormatting.RED)));
        } else {
            context.runOnClient(client -> client.gui.hud.getChat().addClientSystemMessage(Component.literal("Test passed: " + testName).withStyle(ChatFormatting.GREEN)));
        }
    }

    private static GoalProgress getGoalProgress(TestSingleplayerContext singleplayerContext) {
        return singleplayerContext.getServer().computeOnServer(server -> {
            BingoGame game = Objects.requireNonNull(((MinecraftServerExt) server).bingo$getGame());
            return Objects.requireNonNull(game.getGoalProgress(singleplayerContext.getConnection().getServerPlayer(), game.getBoard().getGoals()[0]));
        });
    }

    // Reads a number from the goal's displayed name, e.g. the width in "Activate a 4x5 Nether Portal"
    private static int getGoalNameIntArg(TestSingleplayerContext singleplayerContext, int index) {
        Component name = singleplayerContext.getServer().computeOnServer(server ->
            Objects.requireNonNull(((MinecraftServerExt) server).bingo$getGame()).getBoard().getGoals()[0].name()
        );
        Object arg = ((TranslatableContents) name.getContents()).getArgs()[index];
        return arg instanceof Number number ? number.intValue() : Integer.parseInt(arg instanceof Component component ? component.getString() : arg.toString());
    }

    private static boolean isGameOver(TestSingleplayerContext singleplayerContext) {
        return singleplayerContext.getServer().computeOnServer(server -> ((MinecraftServerExt) server).bingo$getGame() == null);
    }

    private static void waitForGameEnd(TestSingleplayerContext singleplayerContext, int timeout) {
        singleplayerContext.getServer().waitFor(server -> ((MinecraftServerExt) server).bingo$getGame() == null, timeout);
    }

    private static ItemStack enchantedItem(TestSingleplayerContext singleplayerContext, Item item, ResourceKey<Enchantment> enchantment, int level) {
        return singleplayerContext.getServer().computeOnServer(server -> {
            ItemStack stack = new ItemStack(item);
            stack.enchant(server.registryAccess().getOrThrow(enchantment), level);
            return stack;
        });
    }

    private static void setHealth(TestSingleplayerContext singleplayerContext, float health) {
        singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().setHealth(health));
    }

    private static void equip(TestSingleplayerContext singleplayerContext, EquipmentSlot slot, ItemStack stack) {
        singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().setItemSlot(slot, stack));
    }

    private static void dropItem(TestSingleplayerContext singleplayerContext, ItemStack stack, BlockPos pos) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            Vec3 vec = Vec3.atBottomCenterOf(pos);
            level.addFreshEntity(new ItemEntity(level, vec.x, vec.y, vec.z, stack));
        });
    }

    // Waits for an item of the given type to be dropped, then moves the player to it to pick it up
    private static void pickUpDroppedItem(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Item item) {
        singleplayerContext.getServer().waitFor(_ -> !singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypes.ITEM, e -> e.getItem().is(item)).isEmpty(), 20);
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            ItemEntity itemEntity = level.getEntities(EntityTypes.ITEM, e -> e.getItem().is(item)).getFirst();
            teleport(level, singleplayerContext.getConnection().getServerPlayer(), itemEntity.position(), 0, 0);
        });
        singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerPlayer().getInventory().contains(stack -> stack.is(item)), 100);
        context.waitTick();
    }

    private static void give(TestSingleplayerContext singleplayerContext, ItemLike item) {
        give(singleplayerContext, item, 1);
    }

    private static void give(TestSingleplayerContext singleplayerContext, ItemLike item, int count) {
        give(singleplayerContext, new ItemStack(item, count));
    }

    private static void give(TestSingleplayerContext singleplayerContext, ItemStack stack) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            int remaining = stack.getCount();
            while (remaining > 0) {
                int size = Math.min(stack.getMaxStackSize(), remaining);
                remaining -= size;
                ItemStack toAdd = stack.copyWithCount(size);
                if (!player.getInventory().add(toAdd) || !toAdd.isEmpty()) {
                    throw new IllegalStateException("Not enough inventory space to give " + stack);
                }
            }
            player.containerMenu.broadcastChanges();
        });
    }

    private static void clearInventory(TestSingleplayerContext singleplayerContext) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            player.getInventory().clearOrCountMatchingItems(_ -> true, false, -1, player.inventoryMenu.getCraftSlots());
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.slotsChanged(player.getInventory());
        });
    }

    private static void setBlock(TestSingleplayerContext singleplayerContext, BlockPos pos, Block block) {
        setBlock(singleplayerContext, pos, block.defaultBlockState());
    }

    private static void setBlock(TestSingleplayerContext singleplayerContext, BlockPos pos, BlockState state) {
        fill(singleplayerContext, pos, pos, state);
    }

    private static void fill(TestSingleplayerContext singleplayerContext, BlockPos from, BlockPos to, Block block) {
        fill(singleplayerContext, from, to, block.defaultBlockState());
    }

    private static void fill(TestSingleplayerContext singleplayerContext, BlockPos from, BlockPos to, BlockState state) {
        singleplayerContext.getServer().runOnServer(_ -> fill(singleplayerContext.getConnection().getServerLevel(), from, to, state));
    }

    // Same block update behavior as /fill and /setblock
    private static void fill(ServerLevel level, BlockPos from, BlockPos to, BlockState state) {
        record UpdatedPosition(BlockPos pos, BlockState oldState) {
        }

        List<UpdatedPosition> updatedPositions = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            BlockState oldState = level.getBlockState(pos);
            if (level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS)) {
                updatedPositions.add(new UpdatedPosition(pos.immutable(), oldState));
            }
        }
        for (UpdatedPosition updatedPosition : updatedPositions) {
            level.updateNeighboursOnBlockSet(updatedPosition.pos, updatedPosition.oldState);
        }
    }

    private static void placeBed(TestSingleplayerContext singleplayerContext, BlockPos headPos, Block bed) {
        BlockState footState = bed.defaultBlockState();
        Direction facing = footState.getValue(BedBlock.FACING);
        setBlock(singleplayerContext, headPos, footState.setValue(BedBlock.PART, BedPart.HEAD));
        setBlock(singleplayerContext, headPos.relative(facing.getOpposite()), footState);
    }

    private static <T extends Entity> T summon(TestSingleplayerContext singleplayerContext, EntityType<T> type, BlockPos pos) {
        return singleplayerContext.getServer().computeOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            T entity = Objects.requireNonNull(type.create(level, EntitySpawnReason.COMMAND), () -> "Failed to create " + type);
            entity.snapTo(Vec3.atBottomCenterOf(pos), entity.getYRot(), entity.getXRot());
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.COMMAND, null);
            }
            if (!level.tryAddFreshEntityWithPassengers(entity)) {
                throw new IllegalStateException("Failed to add " + entity + " to the level");
            }
            return entity;
        });
    }

    private static void ride(TestSingleplayerContext singleplayerContext, Entity passenger, Entity vehicle) {
        singleplayerContext.getServer().runOnServer(_ -> {
            if (!passenger.startRiding(vehicle, true, true)) {
                throw new IllegalStateException("Failed to make " + passenger + " ride " + vehicle);
            }
        });
    }

    private static void mount(TestSingleplayerContext singleplayerContext, Entity vehicle) {
        ride(singleplayerContext, singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerPlayer()), vehicle);
    }

    private static void damageByPlayer(TestSingleplayerContext singleplayerContext, Entity target, float amount) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            target.hurtServer(level, level.damageSources().playerAttack(singleplayerContext.getConnection().getServerPlayer()), amount);
        });
    }

    private static void teleport(TestSingleplayerContext singleplayerContext, BlockPos pos) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            teleport(player.level(), player, Vec3.atBottomCenterOf(pos), player.getYRot(), player.getXRot());
        });
    }

    private static void teleport(TestSingleplayerContext singleplayerContext, BlockPos pos, float yRot, float xRot) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            teleport(player.level(), player, Vec3.atBottomCenterOf(pos), yRot, xRot);
        });
    }

    private static void teleport(TestSingleplayerContext singleplayerContext, Entity entity, BlockPos pos) {
        singleplayerContext.getServer().runOnServer(_ -> teleport((ServerLevel) entity.level(), entity, Vec3.atBottomCenterOf(pos), entity.getYRot(), entity.getXRot()));
    }

    private static void rotate(TestSingleplayerContext singleplayerContext, float yRot, float xRot) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            teleport(player.level(), player, player.position(), yRot, xRot);
        });
    }

    private static void teleport(ServerLevel level, Entity entity, Vec3 pos, float yRot, float xRot) {
        if (!entity.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), Mth.wrapDegrees(yRot), Mth.wrapDegrees(xRot), true)) {
            throw new IllegalStateException("Failed to teleport " + entity);
        }
    }

    private static void addEffect(TestSingleplayerContext singleplayerContext, Holder<MobEffect> effect, int amplifier) {
        singleplayerContext.getServer().runOnServer(_ ->
            singleplayerContext.getConnection().getServerPlayer().addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, amplifier))
        );
    }

    private static void setTime(TestSingleplayerContext singleplayerContext, ResourceKey<ClockTimeMarker> timeMarker) {
        singleplayerContext.getServer().runOnServer(server -> {
            Holder<WorldClock> clock = singleplayerContext.getConnection().getServerLevel().dimensionType().defaultClock().orElseThrow();
            server.clockManager().moveToTimeMarker(clock, timeMarker);
        });
    }

    private static void setWorldSpawn(TestSingleplayerContext singleplayerContext, BlockPos pos) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            level.setRespawnData(LevelData.RespawnData.of(level.dimension(), pos, 0, 0));
        });
    }

    private static void resetRandomSequence(TestSingleplayerContext singleplayerContext, Identifier sequence, int salt, boolean includeWorldSeed) {
        singleplayerContext.getServer().runOnServer(server ->
            server.getRandomSequences().reset(sequence, singleplayerContext.getConnection().getServerLevel().getSeed(), salt, includeWorldSeed, true)
        );
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> client.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0.0));

        Consumer<WorldCreationUiState> settingsAdjustor = settings -> {
            settings.getGameRules().set(GameRules.RANDOM_TICK_SPEED, 0, null);
            settings.getGameRules().set(GameRules.IMMEDIATE_RESPAWN, true, null);
            settings.updateDimensions((registryAccess, dimensions) -> {
                FlatLevelGeneratorSettings flatSettings = ((FlatLevelSource) dimensions.overworld()).settings();
                List<FlatLayerInfo> layers = List.of(
                    new FlatLayerInfo(1, Blocks.BEDROCK),
                    new FlatLayerInfo(GRASS_Y - DIRT_DEPTH - BEDROCK_Y - 1, Blocks.STONE),
                    new FlatLayerInfo(DIRT_DEPTH, Blocks.DIRT),
                    new FlatLayerInfo(1, Blocks.GRASS_BLOCK)
                );
                FlatLevelGeneratorSettings newSettings = flatSettings.withBiomeAndLayers(layers, flatSettings.structureOverrides(), flatSettings.getBiome());
                return dimensions.replaceOverworldGenerator(registryAccess, new FlatLevelSource(newSettings));
            });
        };
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
