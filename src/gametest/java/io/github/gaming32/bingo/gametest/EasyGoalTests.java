package io.github.gaming32.bingo.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.gaming32.bingo.data.tags.bingo.BingoItemTags;
import io.github.gaming32.bingo.datagen.goal.GoalIds;
import io.github.gaming32.bingo.ext.MinecraftServerExt;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static io.github.gaming32.bingo.gametest.BingoClientGameTest.*;

// Player is at 0, 0, facing south (towards +Z)
@SuppressWarnings("UnstableApiUsage")
final class EasyGoalTests {
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
            setUpFishingPool(singleplayerContext);
            for (int i = 0; i < 2; i++) {
                catchFish(context, singleplayerContext);
            }
            singleplayerContext.getConnection().waitForServerboundPackets();
            context.waitTicks(10);
        });
    }

    @TestGoal
    private static void testFishTreasureAndJunkByHookingItems(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FISH_TREASURE_JUNK, false, () -> {
            give(singleplayerContext, Items.FISHING_ROD);
            singleplayerContext.getConnection().waitForClientboundPackets();
            hookItemEntity(context, singleplayerContext, new ItemStack(Items.NAME_TAG));
            hookItemEntity(context, singleplayerContext, new ItemStack(Items.BOWL));
            context.waitTicks(10);
        });
    }

    @TestGoal
    private static void testFishJunkThenHookTreasureItem(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.Easy.FISH_TREASURE_JUNK, false, () -> {
            // seed 16 gets a junk item on the first fish
            resetRandomSequence(singleplayerContext, BuiltInLootTables.FISHING.identifier(), 16, false);
            setUpFishingPool(singleplayerContext);
            catchFish(context, singleplayerContext);
            singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerPlayer().getInventory().contains(stack -> stack.is(BingoItemTags.FISHING_JUNK)), 100);
            hookItemEntity(context, singleplayerContext, new ItemStack(Items.NAME_TAG));
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

    // Surrounds a pool of water with glass and gives the player a fishing rod to fish in it
    private static void setUpFishingPool(TestSingleplayerContext singleplayerContext) {
        fill(singleplayerContext, new BlockPos(-10, PLAYER_Y, -10), new BlockPos(10, PLAYER_Y + 6, 10), Blocks.GLASS);
        teleport(singleplayerContext, new BlockPos(0, PLAYER_Y + 7, -10));
        fill(singleplayerContext, new BlockPos(-9, PLAYER_Y, -9), new BlockPos(9, PLAYER_Y + 6, 9), Blocks.WATER);
        give(singleplayerContext, enchantedItem(singleplayerContext, Items.FISHING_ROD, Enchantments.LURE, 4));
        singleplayerContext.getConnection().waitForClientboundPackets();
    }

    private static void catchFish(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        singleplayerContext.getServer().waitFor(_ -> {
            FishingHook hook = singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypeTest.forClass(FishingHook.class), _ -> true).getFirst();
            return hook.nibble > 0;
        }, 1000);
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
    }

    // Floats an item entity in front of the player, then hooks it with the held fishing rod and reels it in
    private static void hookItemEntity(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, ItemStack stack) {
        ItemEntity itemEntity = singleplayerContext.getServer().computeOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            ItemEntity entity = new ItemEntity(level, player.getX(), player.getEyeY() - 0.2, player.getZ() + 1.5, stack, 0, 0, 0);
            entity.setNoGravity(true);
            entity.setNeverPickUp();
            level.addFreshEntity(entity);
            return entity;
        });
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        singleplayerContext.getServer().waitFor(_ -> {
            List<? extends FishingHook> hooks = singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypeTest.forClass(FishingHook.class), _ -> true);
            return !hooks.isEmpty() && hooks.getFirst().getHookedIn() == itemEntity;
        }, 20);
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
        singleplayerContext.getServer().runOnServer(_ -> itemEntity.discard());
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
}
