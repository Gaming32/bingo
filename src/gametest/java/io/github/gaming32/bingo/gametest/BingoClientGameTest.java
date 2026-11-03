package io.github.gaming32.bingo.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
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
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
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
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.LevelData;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class BingoClientGameTest implements FabricClientGameTest {
    private static final Logger LOGGER = LogUtils.getLogger();
    // The superflat ground is above sea level, for goals that need to be below sea level
    static final int PLAYER_Y = 65;
    static final int GRASS_Y = PLAYER_Y - 1;
    private static final int DIRT_DEPTH = 2;
    static final int BEDROCK_Y = -64;
    private static final List<String> failedTests = new ArrayList<>();
    private static final List<Class<?>> TEST_CLASSES = List.of(
        VeryEasyGoalTests.class,
        EasyGoalTests.class,
        MediumGoalTests.class,
        HardGoalTests.class,
        VeryHardGoalTests.class
    );

    static void growHugeFungus(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, BlockPos fungusPos, float xRot) {
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

    static void wearArmorThenEquipBoots(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Item helmet, Item chestplate, Item leggings, Item boots) {
        wearArmorThenEquipBoots(context, singleplayerContext, new ItemStack(helmet), new ItemStack(chestplate), new ItemStack(leggings), new ItemStack(boots));
    }

    static void wearArmorThenEquipBoots(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        equip(singleplayerContext, EquipmentSlot.HEAD, helmet);
        equip(singleplayerContext, EquipmentSlot.CHEST, chestplate);
        equip(singleplayerContext, EquipmentSlot.LEGS, leggings);
        give(singleplayerContext, boots);
        singleplayerContext.getConnection().waitForClientboundPackets();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        singleplayerContext.getConnection().waitForServerboundPackets();
    }

    // Gives the items split between two separate stacks
    static void giveSplitStacks(TestSingleplayerContext singleplayerContext, int count, Item... items) {
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

    // Places all but the last block directly, then the player places the last block on top
    static void buildMineralPillar(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, List<Block> blocks) {
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

    static void eat(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Item food) {
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

    // Different effects that don't get in the way of the test
    static final List<Holder<MobEffect>> HARMLESS_EFFECTS = List.of(
        MobEffects.SPEED, MobEffects.SLOWNESS, MobEffects.HASTE, MobEffects.MINING_FATIGUE, MobEffects.STRENGTH,
        MobEffects.JUMP_BOOST, MobEffects.REGENERATION, MobEffects.RESISTANCE, MobEffects.FIRE_RESISTANCE,
        MobEffects.WATER_BREATHING, MobEffects.INVISIBILITY, MobEffects.NIGHT_VISION, MobEffects.WEAKNESS,
        MobEffects.LUCK, MobEffects.SLOW_FALLING
    );

    static final List<ResourceKey<WolfVariant>> WOLF_VARIANTS = List.of(
        WolfVariants.PALE, WolfVariants.SPOTTED, WolfVariants.SNOWY, WolfVariants.BLACK, WolfVariants.ASHEN,
        WolfVariants.RUSTY, WolfVariants.WOODS, WolfVariants.CHESTNUT, WolfVariants.STRIPED
    );

    static void tameWolves(TestSingleplayerContext singleplayerContext, List<ResourceKey<WolfVariant>> variants) {
        for (int i = 0; i < variants.size(); i++) {
            Wolf wolf = summon(singleplayerContext, EntityTypes.WOLF, new BlockPos(i - variants.size() / 2, PLAYER_Y, 3));
            ResourceKey<WolfVariant> variant = variants.get(i);
            singleplayerContext.getServer().runOnServer(server -> {
                wolf.setComponent(DataComponents.WOLF_VARIANT, server.registryAccess().getOrThrow(variant));
                wolf.tame(singleplayerContext.getConnection().getServerPlayer());
            });
        }
    }

    // Trades with the villager the player is looking at, using its first offer
    static void tradeWithVillager(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
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
    }

    static void testGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Identifier goalId, Runnable testRunner) {
        testGoal(context, singleplayerContext, goalId, true, testRunner);
    }

    static void testGoal(
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

    static void testNeverGoal(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Identifier goalId, Runnable testRunner) {
        testNeverGoal(context, singleplayerContext, goalId, true, testRunner);
    }

    static void testNeverGoal(
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
    static void restoreGround(ServerLevel level, BlockPos from, BlockPos to) {
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

    static void restoreGround(TestSingleplayerContext singleplayerContext, BlockPos from, BlockPos to) {
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

    static GoalProgress getGoalProgress(TestSingleplayerContext singleplayerContext) {
        return singleplayerContext.getServer().computeOnServer(server -> {
            BingoGame game = Objects.requireNonNull(((MinecraftServerExt) server).bingo$getGame());
            return Objects.requireNonNull(game.getGoalProgress(singleplayerContext.getConnection().getServerPlayer(), game.getBoard().getGoals()[0]));
        });
    }

    // Reads a number from the goal's displayed name, e.g. the width in "Activate a 4x5 Nether Portal"
    static int getGoalNameIntArg(TestSingleplayerContext singleplayerContext, int index) {
        Component name = singleplayerContext.getServer().computeOnServer(server ->
            Objects.requireNonNull(((MinecraftServerExt) server).bingo$getGame()).getBoard().getGoals()[0].name()
        );
        Object arg = ((TranslatableContents) name.getContents()).getArgs()[index];
        return arg instanceof Number number ? number.intValue() : Integer.parseInt(arg instanceof Component component ? component.getString() : arg.toString());
    }

    static boolean isGameOver(TestSingleplayerContext singleplayerContext) {
        return singleplayerContext.getServer().computeOnServer(server -> ((MinecraftServerExt) server).bingo$getGame() == null);
    }

    static void waitForGameEnd(TestSingleplayerContext singleplayerContext, int timeout) {
        singleplayerContext.getServer().waitFor(server -> ((MinecraftServerExt) server).bingo$getGame() == null, timeout);
    }

    static ItemStack enchantedItem(TestSingleplayerContext singleplayerContext, Item item, ResourceKey<Enchantment> enchantment, int level) {
        return singleplayerContext.getServer().computeOnServer(server -> {
            ItemStack stack = new ItemStack(item);
            stack.enchant(server.registryAccess().getOrThrow(enchantment), level);
            return stack;
        });
    }

    static void setHealth(TestSingleplayerContext singleplayerContext, float health) {
        singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().setHealth(health));
    }

    static void equip(TestSingleplayerContext singleplayerContext, EquipmentSlot slot, ItemStack stack) {
        singleplayerContext.getServer().runOnServer(_ -> singleplayerContext.getConnection().getServerPlayer().setItemSlot(slot, stack));
    }

    static void dropItem(TestSingleplayerContext singleplayerContext, ItemStack stack, BlockPos pos) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            Vec3 vec = Vec3.atBottomCenterOf(pos);
            level.addFreshEntity(new ItemEntity(level, vec.x, vec.y, vec.z, stack));
        });
    }

    // Waits for an item of the given type to be dropped, then moves the player to it to pick it up
    static void pickUpDroppedItem(ClientGameTestContext context, TestSingleplayerContext singleplayerContext, Item item) {
        singleplayerContext.getServer().waitFor(_ -> !singleplayerContext.getConnection().getServerLevel().getEntities(EntityTypes.ITEM, e -> e.getItem().is(item)).isEmpty(), 20);
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            ItemEntity itemEntity = level.getEntities(EntityTypes.ITEM, e -> e.getItem().is(item)).getFirst();
            teleport(level, singleplayerContext.getConnection().getServerPlayer(), itemEntity.position(), 0, 0);
        });
        singleplayerContext.getServer().waitFor(_ -> singleplayerContext.getConnection().getServerPlayer().getInventory().contains(stack -> stack.is(item)), 100);
        context.waitTick();
    }

    static void give(TestSingleplayerContext singleplayerContext, ItemLike item) {
        give(singleplayerContext, item, 1);
    }

    static void give(TestSingleplayerContext singleplayerContext, ItemLike item, int count) {
        give(singleplayerContext, new ItemStack(item, count));
    }

    static void give(TestSingleplayerContext singleplayerContext, ItemStack stack) {
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

    static void clearInventory(TestSingleplayerContext singleplayerContext) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            player.getInventory().clearOrCountMatchingItems(_ -> true, false, -1, player.inventoryMenu.getCraftSlots());
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.slotsChanged(player.getInventory());
        });
    }

    static void setBlock(TestSingleplayerContext singleplayerContext, BlockPos pos, Block block) {
        setBlock(singleplayerContext, pos, block.defaultBlockState());
    }

    static void setBlock(TestSingleplayerContext singleplayerContext, BlockPos pos, BlockState state) {
        fill(singleplayerContext, pos, pos, state);
    }

    static void fill(TestSingleplayerContext singleplayerContext, BlockPos from, BlockPos to, Block block) {
        fill(singleplayerContext, from, to, block.defaultBlockState());
    }

    static void fill(TestSingleplayerContext singleplayerContext, BlockPos from, BlockPos to, BlockState state) {
        singleplayerContext.getServer().runOnServer(_ -> fill(singleplayerContext.getConnection().getServerLevel(), from, to, state));
    }

    // Same block update behavior as /fill and /setblock
    static void fill(ServerLevel level, BlockPos from, BlockPos to, BlockState state) {
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

    static void placeBed(TestSingleplayerContext singleplayerContext, BlockPos headPos, Block bed) {
        BlockState footState = bed.defaultBlockState();
        Direction facing = footState.getValue(BedBlock.FACING);
        setBlock(singleplayerContext, headPos, footState.setValue(BedBlock.PART, BedPart.HEAD));
        setBlock(singleplayerContext, headPos.relative(facing.getOpposite()), footState);
    }

    static <T extends Entity> T summon(TestSingleplayerContext singleplayerContext, EntityType<T> type, BlockPos pos) {
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

    static void ride(TestSingleplayerContext singleplayerContext, Entity passenger, Entity vehicle) {
        singleplayerContext.getServer().runOnServer(_ -> {
            if (!passenger.startRiding(vehicle, true, true)) {
                throw new IllegalStateException("Failed to make " + passenger + " ride " + vehicle);
            }
        });
    }

    static void mount(TestSingleplayerContext singleplayerContext, Entity vehicle) {
        ride(singleplayerContext, singleplayerContext.getServer().computeOnServer(_ -> singleplayerContext.getConnection().getServerPlayer()), vehicle);
    }

    static void damageByPlayer(TestSingleplayerContext singleplayerContext, Entity target, float amount) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            target.hurtServer(level, level.damageSources().playerAttack(singleplayerContext.getConnection().getServerPlayer()), amount);
        });
    }

    static void teleport(TestSingleplayerContext singleplayerContext, BlockPos pos) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            teleport(player.level(), player, Vec3.atBottomCenterOf(pos), player.getYRot(), player.getXRot());
        });
    }

    static void teleport(TestSingleplayerContext singleplayerContext, BlockPos pos, float yRot, float xRot) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            teleport(player.level(), player, Vec3.atBottomCenterOf(pos), yRot, xRot);
        });
    }

    static void teleport(TestSingleplayerContext singleplayerContext, Entity entity, BlockPos pos) {
        singleplayerContext.getServer().runOnServer(_ -> teleport((ServerLevel) entity.level(), entity, Vec3.atBottomCenterOf(pos), entity.getYRot(), entity.getXRot()));
    }

    static void rotate(TestSingleplayerContext singleplayerContext, float yRot, float xRot) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerPlayer player = singleplayerContext.getConnection().getServerPlayer();
            teleport(player.level(), player, player.position(), yRot, xRot);
        });
    }

    static void teleport(ServerLevel level, Entity entity, Vec3 pos, float yRot, float xRot) {
        if (!entity.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), Mth.wrapDegrees(yRot), Mth.wrapDegrees(xRot), true)) {
            throw new IllegalStateException("Failed to teleport " + entity);
        }
    }

    static void addEffect(TestSingleplayerContext singleplayerContext, Holder<MobEffect> effect, int amplifier) {
        singleplayerContext.getServer().runOnServer(_ ->
            singleplayerContext.getConnection().getServerPlayer().addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, amplifier))
        );
    }

    static void setTime(TestSingleplayerContext singleplayerContext, ResourceKey<ClockTimeMarker> timeMarker) {
        singleplayerContext.getServer().runOnServer(server -> {
            Holder<WorldClock> clock = singleplayerContext.getConnection().getServerLevel().dimensionType().defaultClock().orElseThrow();
            server.clockManager().moveToTimeMarker(clock, timeMarker);
        });
    }

    static void setWorldSpawn(TestSingleplayerContext singleplayerContext, BlockPos pos) {
        singleplayerContext.getServer().runOnServer(_ -> {
            ServerLevel level = singleplayerContext.getConnection().getServerLevel();
            level.setRespawnData(LevelData.RespawnData.of(level.dimension(), pos, 0, 0));
        });
    }

    static void resetRandomSequence(TestSingleplayerContext singleplayerContext, Identifier sequence, int salt, boolean includeWorldSeed) {
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

            for (Method method : TEST_CLASSES.stream().flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods())).toList()) {
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
                        method.setAccessible(true);
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
    @interface TestGoal {
        boolean onlyMe() default false;
        TestDimension dimension() default TestDimension.OVERWORLD;
    }

    enum TestDimension {
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
