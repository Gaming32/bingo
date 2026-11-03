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
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.boat.Boat;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.block.ShelfMushroomBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.PotentSulfurState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gamerules.GameRules;
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
import java.util.Set;
import java.util.function.Consumer;

// Player is at 0, 0, facing south (towards +Z)
@SuppressWarnings("UnstableApiUsage")
public class BingoClientGameTest implements FabricClientGameTest {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PLAYER_Y = -60;
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
            setTime(singleplayerContext, ClockTimeMarkers.DAY);
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
                clearEffects(singleplayerContext);
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
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerLevel level = singleplayerContext.getConnection().getServerLevel();
                ItemEntity item = new ItemEntity(level, 0.5, PLAYER_Y, 0.5, new ItemStack(Items.CRAFTING_TABLE));
                level.addFreshEntity(item);
            });
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
                clearEffects(singleplayerContext);
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
            ItemStack fishingRod = singleplayerContext.getServer().computeOnServer(server -> {
                ItemStack stack = new ItemStack(Items.FISHING_ROD);
                stack.enchant(server.registryAccess().getOrThrow(Enchantments.LURE), 4);
                return stack;
            });
            give(singleplayerContext, fishingRod);
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
            try {
                setBlock(singleplayerContext, new BlockPos(0, 128, 1), Blocks.OBSIDIAN);
                placeBed(singleplayerContext, new BlockPos(0, 128, 2), Blocks.BED.white());
                rotate(singleplayerContext, 0, 20);
                singleplayerContext.getConnection().waitForClientboundPackets();
                context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
                singleplayerContext.getConnection().waitForServerboundPackets();
            } finally {
                singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().setHealth(20));
            }
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
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
                for (ItemStack itemCost : itemCosts) {
                    player.getInventory().add(itemCost.copy());
                }
            });
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
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
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
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
                player.getInventory().setItem(EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_HELMET, 0xff0000));
                player.getInventory().setItem(EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_CHESTPLATE, 0x00ff00));
                player.getInventory().setItem(EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_LEGGINGS, 0x0000ff));
                player.getInventory().add(new ItemStack(Items.LEATHER_BOOTS));
            });
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
        });
    }

    @TestGoal
    private static void testWearDifferentColoredArmorNotWearing(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.WEAR_DIFFERENT_COLORED_ARMOR, false, () -> {
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
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
            singleplayerContext.getServer().runOnServer(_ -> {
                ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
                player.getInventory().setItem(EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_HELMET, 0xff0000));
                player.getInventory().setItem(EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_CHESTPLATE, 0xff0000));
                player.getInventory().setItem(EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_LEGGINGS, 0xff0000));
                player.getInventory().setItem(EquipmentSlot.FEET.getIndex(Inventory.INVENTORY_SIZE), makeColoredArmor(Items.LEATHER_BOOTS, 0xff0000));
            });
            singleplayerContext.getConnection().waitForClientboundPackets();
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            singleplayerContext.getConnection().waitForServerboundPackets();
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
            Zombie zombie = singleplayerContext.getServer().computeOnServer(server -> {
                Zombie z = new Zombie(server.overworld());
                z.snapTo(Vec3.atBottomCenterOf(new BlockPos(0, PLAYER_Y, 2)));
                server.overworld().addFreshEntity(z);
                return z;
            });
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

    // TODO: test medium, hard, and very goals

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
            setTime(singleplayerContext, ClockTimeMarkers.DAY);
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
            setTime(singleplayerContext, ClockTimeMarkers.DAY);
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
        context.runOnClient(client -> Objects.requireNonNull(client.player).setDeltaMovement(Vec3.ZERO));
        singleplayerContext.getServer().runOnServer(server -> {
            ServerLevel level = Objects.requireNonNull(server.getLevel(dimension.key));
            teleport(level, singleplayerContext.getConnection().getServerPlayer(), Vec3.atBottomCenterOf(dimension.spawnPos), 0, 0);
        });
        singleplayerContext.getConnection().waitForClientboundPackets();
        singleplayerContext.getConnection().waitForChunksDownload();
        clearInventory(singleplayerContext);
        singleplayerContext.getServer().runOnServer(server -> {
            ServerLevel level = Objects.requireNonNull(server.getLevel(dimension.key));
            fill(level, dimension.spawnPos.offset(-16, 0, -16), dimension.spawnPos.offset(16, 16, 16), Blocks.AIR.defaultBlockState());
            for (Entity entity : level.getAllEntities()) {
                // idk why entity can be null here, but it can
                if (entity != null && !(entity instanceof Player)) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        });
        singleplayerContext.getConnection().waitForClientboundPackets();
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

    private static void clearEffects(TestSingleplayerContext singleplayerContext) {
        singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().removeAllEffects());
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
