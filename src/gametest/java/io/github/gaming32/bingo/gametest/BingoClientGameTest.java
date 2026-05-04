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
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.FailablePredicate;
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
import java.util.List;
import java.util.Objects;

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
        context.runOnClient(client -> Objects.requireNonNull(client.player).setDeltaMovement(Vec3.ZERO));
        singleplayerContext.getServer().runCommand("clear @a");
        singleplayerContext.getServer().runCommand("fill -16 " + PLAYER_Y + " -16 16 " + (PLAYER_Y + 16) + " 16 air");
        singleplayerContext.getServer().runCommand("tp @a 0 " + PLAYER_Y + " 0 0 0");
        singleplayerContext.getServer().runOnServer(server -> {
            for (Entity entity : server.overworld().getAllEntities()) {
                if (!(entity instanceof Player)) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        });
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

        try (TestSingleplayerContext singleplayerContext = context.worldBuilder().create()) {
            singleplayerContext.getServer().runCommand("bingo teams create red");
            singleplayerContext.getServer().runCommand("bingo teams randomize");

            failedTests.clear();

            for (Method method : getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(TestGoal.class)) {
                    if (!Modifier.isStatic(method.getModifiers())) {
                        throw new IllegalStateException("Goal test methods must be static");
                    }
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
    }
}
