package io.github.gaming32.bingo.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.gaming32.bingo.datagen.goal.GoalIds;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConduitBlock;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.github.gaming32.bingo.gametest.BingoClientGameTest.*;

// Player is at 0, 0, facing south (towards +Z)
@SuppressWarnings("UnstableApiUsage")
final class VeryHardGoalTests {
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
    private static void testAllMineralBlocks(ClientGameTestContext context, TestSingleplayerContext singleplayerContext) {
        testGoal(context, singleplayerContext, GoalIds.VeryHard.ALL_MINERAL_BLOCKS, () -> {
            buildMineralPillar(context, singleplayerContext, List.of(
                Blocks.COAL_BLOCK, Blocks.COPPER_BLOCK.weathering().unaffected(), Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK,
                Blocks.REDSTONE_BLOCK, Blocks.LAPIS_BLOCK, Blocks.EMERALD_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.NETHERITE_BLOCK
            ));
        });
    }
}
