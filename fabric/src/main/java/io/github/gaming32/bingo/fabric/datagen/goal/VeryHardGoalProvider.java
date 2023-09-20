package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.conditions.EndermanHasOnlyBeenDamagedByEndermiteCondition;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoSub;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.BingoDimensionTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.ext.LocationPredicateExt;
import io.github.gaming32.bingo.triggers.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.Arrays;
import java.util.function.Consumer;

public class VeryHardGoalProvider extends DifficultyGoalProvider {
    public VeryHardGoalProvider(Consumer<BingoGoal> goalAdder) {
        super(4, "very_hard/", goalAdder);
    }

    @Override
    public void addGoals() {
        addGoal(obtainSomeItemsFromTag(id("ores"), Items.DIAMOND_ORE, BingoItemTags.ORES, "bingo.goal.ores", 5, 7)
            .tooltip(Component.translatable("bingo.goal.ores.tooltip"))
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("different_potions"))
            .sub("count", BingoSub.random(12, 15))
            .criterion("potions",
                DifferentPotionsTrigger.TriggerInstance.differentPotions(0),
                subber -> subber.sub("conditions.min_count", "count")
            )
            .tags(BingoTags.ITEM, BingoTags.NETHER, BingoTags.COMBAT, BingoTags.OVERWORLD)
            .reactant("pacifist")
            .name(
                Component.translatable("bingo.goal.different_potions", 0),
                subber -> subber.sub("with.0", "count")
            )
            .tooltip(Component.translatable("bingo.goal.different_potions.tooltip"))
            .icon(
                PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.MUNDANE),
                subber -> subber.sub("count", "count")
            ));
        addGoal(allSomethingsGoal("chestplates", ArmorItem.class, i -> i.getType() == ArmorItem.Type.CHESTPLATE)
            .icon(Items.NETHERITE_CHESTPLATE)
        );
        addGoal(obtainItemGoal(
            id("any_head"),
            new ItemStack(Items.ZOMBIE_HEAD),
            ItemPredicate.Builder.item().of(
                Items.SKELETON_SKULL,
                Items.PLAYER_HEAD,
                Items.ZOMBIE_HEAD,
                Items.CREEPER_HEAD,
                Items.DRAGON_HEAD,
                Items.PIGLIN_HEAD
            ))
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.any_head"))
            .tooltip(Component.translatable("bingo.goal.any_head.tooltip")));

        addGoal(BingoGoal.builder(id("all_dyes"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Arrays.stream(DyeColor.values()).map(DyeItem::byColor).toArray(ItemLike[]::new)))
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.all_dyes"))
            .tooltip(Component.translatable("bingo.sixteen_bang",
                Arrays.stream(DyeColor.values()).map(color -> Component.translatable("color.minecraft." + color.getName())).toArray(Object[]::new)))
            .icon(new ItemStack(Items.RED_DYE, 16))
            .antisynergy("every_color")
            .reactant("use_furnace"));
        addGoal(BingoGoal.builder(id("levels"))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(50)).build())
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 50))
            .icon(new ItemStack(Items.EXPERIENCE_BOTTLE, 50))
            .infrequency(2)
            .antisynergy("levels"));
        addGoal(obtainItemGoal(id("tipped_arrow"), Items.TIPPED_ARROW, 16, 32)
            .tags(BingoTags.NETHER, BingoTags.OVERWORLD)
            .icon(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.MUNDANE), subber -> subber.sub("count", "count")));
        addGoal(mineralPillarGoal(id("all_mineral_blocks"), BingoBlockTags.ALL_MINERAL_BLOCKS)
            .name(Component.translatable("bingo.goal.all_mineral_blocks"))
            .tooltip(Component.translatable("bingo.goal.all_mineral_blocks.tooltip"))
            .tags(BingoTags.OVERWORLD, BingoTags.NETHER)
            .icon(Blocks.NETHERITE_BLOCK)
        );
        addGoal(BingoGoal.builder(id("sleep_in_mansion"))
            .criterion("sleep", new PlayerTrigger.TriggerInstance(
                CriteriaTriggers.SLEPT_IN_BED.getId(),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().located(LocationPredicate.inStructure(BuiltinStructures.WOODLAND_MANSION)).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.sleep_in_mansion"))
            .icon(Items.BROWN_BED));
        addGoal(obtainItemGoal(id("mycelium"), Items.MYCELIUM, 10, 32)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("coral_blocks"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK,
                Items.FIRE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK
            ))
            .tags(BingoTags.ITEM, BingoTags.RARE_BIOME, BingoTags.OCEAN, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.coral_blocks"))
            .icon(Blocks.BRAIN_CORAL_BLOCK));
        addGoal(obtainItemGoal(id("blue_ice"), Items.BLUE_ICE, 32, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("full_power_conduit"))
            .criterion("power", PowerConduitTrigger.TriggerInstance.powerConduit(MinMaxBounds.Ints.exactly(6)))
            .tags(BingoTags.BUILD, BingoTags.OCEAN, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.full_power_conduit"))
            .icon(Blocks.CONDUIT));
        addGoal(BingoGoal.builder(id("all_diamond_craftables"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.DIAMOND_BLOCK, Items.DIAMOND_AXE, Items.DIAMOND_BOOTS,
                Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET, Items.DIAMOND_HOE,
                Items.DIAMOND_LEGGINGS, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL,
                Items.DIAMOND_SWORD, Items.ENCHANTING_TABLE, Items.FIREWORK_STAR, Items.JUKEBOX))
            .name(Component.translatable("bingo.goal.all_diamond_craftables"))
            .tooltip(Component.translatable("bingo.goal.all_diamond_craftables.tooltip"))
            .icon(Items.DIAMOND_HOE)
            .antisynergy("diamond_items"));
        addGoal(BingoGoal.builder(id("shulker_in_overworld"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity(
                EntityPredicate.Builder.entity().of(EntityType.SHULKER).located(LocationPredicateExt.inDimension(BingoDimensionTags.OVERWORLDS))))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.END, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.shulker_in_overworld"))
            .icon(Items.SHULKER_SHELL)
            .reactant("pacifist"));
        addGoal(obtainItemGoal(id("diamond_block"), Items.DIAMOND_BLOCK, 5, 10)
            .infrequency(2));
        addGoal(BingoGoal.builder(id("complete_full_size_end_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap(
                MinMaxBounds.Ints.atLeast(MapItemSavedData.MAX_SCALE),
                LocationPredicateExt.inDimension(BingoDimensionTags.ENDS)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.END)
            .name(Component.translatable("bingo.goal.complete_full_size_end_map"))
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map"));
        addGoal(obtainItemGoal(id("wither_rose"), Items.WITHER_ROSE, 32, 64)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        addGoal(BingoGoal.builder(id("panda_slime_ball"))
            // Currently untested. They have a 1/175,000 or a 1/2,100,000 chance to drop one on a tick.
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.SLIME_BALL).build(),
                EntityPredicate.Builder.entity().of(EntityType.PANDA).build()
            ))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name(Component.translatable("bingo.goal.panda_slime_ball"))
            .icon(Items.SLIME_BALL));
        addGoal(obtainItemGoal(id("netherite_block"), Items.NETHERITE_BLOCK, 2, 2)
            .tags(BingoTags.NETHER));
        addGoal(BingoGoal.builder(id("full_netherite_armor_and_tools"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET,
                Items.NETHERITE_SWORD, Items.NETHERITE_SHOVEL, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_HOE
            ))
            .tags(BingoTags.ITEM, BingoTags.NETHER)
            .name(Component.translatable("bingo.goal.full_netherite_armor_and_tools"))
            .icon(Items.NETHERITE_HOE));
        addGoal(BingoGoal.builder(id("zombify_pig"))
            .criterion("channeling", ZombifyPigTrigger.zombifyPig()
                .direct(true)
                .build()
            )
            .criterion("nearby", ZombifyPigTrigger.zombifyPig()
                .pig(EntityPredicate.Builder.entity()
                    .distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(16)))
                    .build()
                )
                .direct(false)
                .build()
            )
            .requirements(RequirementsStrategy.OR)
            .name(Component.translatable("bingo.goal.zombify_pig"))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(Items.COOKED_PORKCHOP));
        addGoal(obtainItemGoal(id("trident"), Items.TRIDENT)
            .tags(BingoTags.OCEAN, BingoTags.COMBAT, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("party_parrots"))
            .criterion("party", PartyParrotsTrigger.TriggerInstance.partyParrots())
            .name(Component.translatable("bingo.goal.party_parrots"))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .icon(Items.MUSIC_DISC_CAT));
        addGoal(bedRowGoal(id("bed_row"), 16, 16)
            .reactant("use_furnace")
            .antisynergy("every_color")
            .infrequency(2)
            .tags(BingoTags.ACTION)
            .tooltip(Component.translatable("bingo.sixteen_bang",
                Arrays.stream(DyeColor.values()).map(color -> Component.translatable("color.minecraft." + color.getName())).toArray(Object[]::new))));
        addGoal(BingoGoal.builder(id("kill_enderman_with_endermites"))
            .criterion("obtain", EntityDieNearPlayerTrigger.builder()
                .entity(ContextAwarePredicate.create(
                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(EntityType.ENDERMAN)).build(),
                    new EndermanHasOnlyBeenDamagedByEndermiteCondition()))
                .killingBlow(DamagePredicate.Builder.damageInstance().sourceEntity(EntityPredicate.Builder.entity().of(EntityType.ENDERMITE).build()).build())
                .build())
            .name(Component.translatable("bingo.goal.kill_enderman_with_endermites"))
            .tooltip(Component.translatable("bingo.goal.kill_enderman_with_endermites.tooltip"))
            .icon(Items.ENDERMITE_SPAWN_EGG)
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.END));
        addGoal(BingoGoal.builder(id("beacon_regen"))
            .criterion("effect", BeaconEffectTrigger.TriggerInstance.effectApplied(MobEffects.REGENERATION))
            .tags(BingoTags.ITEM, BingoTags.NETHER, BingoTags.OVERWORLD, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.beacon_regen"))
            .icon(Blocks.BEACON)
            .reactant("pacifist"));
    }
}
