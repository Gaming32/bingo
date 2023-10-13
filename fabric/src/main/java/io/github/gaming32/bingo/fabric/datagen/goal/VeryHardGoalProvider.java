package io.github.gaming32.bingo.fabric.datagen.goal;

import com.google.common.collect.ImmutableList;
import io.github.gaming32.bingo.conditions.EndermanHasOnlyBeenDamagedByEndermiteCondition;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.ProgressTracker;
import io.github.gaming32.bingo.data.icons.*;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.triggers.*;
import net.minecraft.Util;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class VeryHardGoalProvider extends DifficultyGoalProvider {
    public VeryHardGoalProvider(Consumer<BingoGoal> goalAdder) {
        super(4, "very_hard/", goalAdder);
    }

    @Override
    public void addGoals() {
        addGoal(obtainSomeItemsFromTag(id("ores"), BingoItemTags.ORES, "bingo.goal.ores", 5, 7)
            .tooltip(Component.translatable("bingo.goal.ores.tooltip"))
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("different_potions"))
            .sub("count", BingoSub.random(12, 15))
            .criterion("potions",
                DifferentPotionsTrigger.TriggerInstance.differentPotions(0),
                subber -> subber.sub("conditions.min_count", "count")
            )
            .progress("potions")
            .tags(BingoTags.ITEM, BingoTags.NETHER, BingoTags.COMBAT, BingoTags.OVERWORLD)
            .reactant("pacifist")
            .name(
                Component.translatable("bingo.goal.different_potions", 0),
                subber -> subber.sub("with.0", "count")
            )
            .tooltip(Component.translatable("bingo.goal.different_potions.tooltip"))
            .icon(
                createPotionsIcon(Items.POTION),
                subber -> subber.multiSub("value.*.value.Count", "count")
            )
        );
        addGoal(obtainAllItemsFromTag(BingoItemTags.ARMOR_CHESTPLATES, "chestplates")
            .tags(BingoTags.NETHER)
            .tooltip(Component.translatable("bingo.goal.all_somethings.tooltip")));
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
            .tooltip(Component.translatable(
                "bingo.sixteen_bang",
                Arrays.stream(DyeColor.values()).map(color -> Component.translatable("color.minecraft." + color.getName())).toArray(Object[]::new)
            ))
            .icon(new CycleIcon(
                Arrays.stream(DyeColor.values())
                    .map(DyeItem::byColor)
                    .map(i -> new ItemStack(i, 16))
                    .map(ItemIcon::new)
                    .collect(ImmutableList.toImmutableList())
            ))
            .antisynergy("every_color")
            .reactant("use_furnace")
        );
        addGoal(BingoGoal.builder(id("levels"))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(50)).build())
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 50))
            .icon(new ItemStack(Items.EXPERIENCE_BOTTLE, 50))
            .infrequency(2)
            .antisynergy("levels"));
        addGoal(obtainItemGoal(id("tipped_arrow"), Items.TIPPED_ARROW, 16, 32)
            .tags(BingoTags.NETHER, BingoTags.OVERWORLD)
            .icon(
                createPotionsIcon(Items.TIPPED_ARROW),
                subber -> subber.multiSub("value.*.value.Count", "count")
            )
        );
        addGoal(mineralPillarGoal(id("all_mineral_blocks"), BingoBlockTags.ALL_MINERAL_BLOCKS)
            .name(Component.translatable("bingo.goal.all_mineral_blocks"))
            .tooltip(Component.translatable("bingo.goal.all_mineral_blocks.tooltip"))
            .tags(BingoTags.OVERWORLD, BingoTags.NETHER)
            .icon(new ItemTagCycleIcon(BingoItemTags.ALL_MINERAL_BLOCKS))
        );
        addGoal(BingoGoal.builder(id("sleep_in_mansion"))
            .criterion("sleep", CriteriaTriggers.SLEPT_IN_BED.createCriterion(
                new PlayerTrigger.TriggerInstance(
                    Optional.of(EntityPredicate.wrap(
                        EntityPredicate.Builder.entity()
                            .located(LocationPredicate.Builder.inStructure(BuiltinStructures.WOODLAND_MANSION))
                            .build()
                    ))
                )
            ))
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
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.TUBE_CORAL_BLOCK),
                ItemIcon.ofItem(Items.BRAIN_CORAL_BLOCK),
                ItemIcon.ofItem(Items.BUBBLE_CORAL_BLOCK),
                ItemIcon.ofItem(Items.FIRE_CORAL_BLOCK),
                ItemIcon.ofItem(Items.HORN_CORAL_BLOCK)
            ))
        );
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
                Items.DIAMOND_SWORD, Items.ENCHANTING_TABLE, Items.FIREWORK_STAR, Items.JUKEBOX
            ))
            .name(Component.translatable("bingo.goal.all_diamond_craftables"))
            .tooltip(Component.translatable("bingo.goal.all_diamond_craftables.tooltip"))
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.DIAMOND_BLOCK),
                ItemIcon.ofItem(Items.DIAMOND_AXE),
                ItemIcon.ofItem(Items.DIAMOND_BOOTS),
                ItemIcon.ofItem(Items.DIAMOND_CHESTPLATE),
                ItemIcon.ofItem(Items.DIAMOND_HELMET),
                ItemIcon.ofItem(Items.DIAMOND_HOE),
                ItemIcon.ofItem(Items.DIAMOND_LEGGINGS),
                ItemIcon.ofItem(Items.DIAMOND_PICKAXE),
                ItemIcon.ofItem(Items.DIAMOND_SHOVEL),
                ItemIcon.ofItem(Items.DIAMOND_SWORD),
                ItemIcon.ofItem(Items.ENCHANTING_TABLE),
                ItemIcon.ofItem(Items.FIREWORK_STAR),
                ItemIcon.ofItem(Items.JUKEBOX)
            ))
            .antisynergy("diamond_items")
        );
        addGoal(BingoGoal.builder(id("shulker_in_overworld"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity(
                EntityPredicate.Builder.entity()
                    .of(EntityType.SHULKER)
                    .located(LocationPredicate.Builder.inDimension(Level.OVERWORLD))
            ))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.END, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.shulker_in_overworld"))
            .icon(Items.SHULKER_SHELL)
            .reactant("pacifist"));
        addGoal(obtainItemGoal(id("diamond_block"), Items.DIAMOND_BLOCK, 5, 10)
            .infrequency(2));
        addGoal(BingoGoal.builder(id("complete_full_size_end_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap(
                MinMaxBounds.Ints.atLeast(MapItemSavedData.MAX_SCALE),
                LocationPredicate.Builder.inDimension(Level.END).build()
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
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.NETHERITE_BOOTS),
                ItemIcon.ofItem(Items.NETHERITE_LEGGINGS),
                ItemIcon.ofItem(Items.NETHERITE_CHESTPLATE),
                ItemIcon.ofItem(Items.NETHERITE_HELMET),
                ItemIcon.ofItem(Items.NETHERITE_SWORD),
                ItemIcon.ofItem(Items.NETHERITE_SHOVEL),
                ItemIcon.ofItem(Items.NETHERITE_PICKAXE),
                ItemIcon.ofItem(Items.NETHERITE_AXE),
                ItemIcon.ofItem(Items.NETHERITE_HOE)
            ))
        );
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
            .requirements(AdvancementRequirements.Strategy.OR)
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
            .tooltip(Component.translatable(
                "bingo.sixteen_bang",
                Arrays.stream(DyeColor.values()).map(color -> Component.translatable("color.minecraft." + color.getName())).toArray(Object[]::new)
            ))
        );
        addGoal(BingoGoal.builder(id("kill_enderman_with_endermites"))
            .criterion("obtain", EntityDieNearPlayerTrigger.builder()
                .entity(ContextAwarePredicate.create(
                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(EntityType.ENDERMAN)).build(),
                    EndermanHasOnlyBeenDamagedByEndermiteCondition.INSTANCE
                ))
                .killingBlow(DamagePredicate.Builder.damageInstance().sourceEntity(EntityPredicate.Builder.entity().of(EntityType.ENDERMITE).build()).build())
                .build()
            )
            .name(Component.translatable("bingo.goal.kill_enderman_with_endermites"))
            .tooltip(Component.translatable("bingo.goal.kill_enderman_with_endermites.tooltip"))
            .icon(EntityIcon.ofSpawnEgg(EntityType.ENDERMITE))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.END));
        addGoal(BingoGoal.builder(id("beacon_regen"))
            .criterion("effect", BeaconEffectTrigger.TriggerInstance.effectApplied(MobEffects.REGENERATION))
            .tags(BingoTags.ITEM, BingoTags.NETHER, BingoTags.OVERWORLD, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.beacon_regen"))
            .icon(Blocks.BEACON)
            .reactant("pacifist"));
        addGoal(obtainSomeItemsFromTag(id("armor_trims"), ItemTags.TRIM_TEMPLATES, "bingo.goal.armor_trims", 5, 5)
            .antisynergy("armor_trims"));
        addGoal(obtainAllGoatHorns());

        EntityType<?>[] tamableAnimals = {
            EntityType.ALLAY,
            EntityType.AXOLOTL,
            EntityType.CAMEL,
            EntityType.CAT,
            EntityType.DONKEY,
            EntityType.FOX,
            EntityType.HORSE,
            EntityType.LLAMA,
            EntityType.MULE,
            EntityType.OCELOT,
            EntityType.PARROT,
            EntityType.SKELETON_HORSE,
            EntityType.STRIDER,
            EntityType.TRADER_LLAMA,
            EntityType.WOLF,
        };
    }

    private BingoGoal.Builder obtainAllGoatHorns() {
        List<ResourceKey<Instrument>> goatHornInstruments = List.copyOf(BuiltInRegistries.INSTRUMENT.registryKeySet());
        List<ItemStack> goatHorns = goatHornInstruments.stream().map(instrument -> InstrumentItem.create(Items.GOAT_HORN, Holder.Reference.createStandAlone(BuiltInRegistries.INSTRUMENT.holderOwner(), instrument))).toList();
        MutableComponent tooltip = Component.translatable(Util.makeDescriptionId("instrument", goatHornInstruments.get(0).location()));
        for (int i = 1; i < goatHornInstruments.size(); i++) {
            tooltip.append(", ").append(Component.translatable(Util.makeDescriptionId("instrument", goatHornInstruments.get(i).location())));
        }
        BingoGoal.Builder builder = BingoGoal.builder(id("all_goat_horns"));
        for (int i = 0; i < goatHornInstruments.size(); i++) {
            ResourceKey<Instrument> instrument = goatHornInstruments.get(i);
            ItemStack goatHorn = goatHorns.get(i);
            builder.criterion(
                "obtain_" + instrument.location().getNamespace() + "_" + instrument.location().getPath(),
                InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(goatHorn.getItem()).hasNbt(goatHorn.getTag()).build())
            );
        }
        return builder
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.all_goat_horns"))
            .tooltip(tooltip)
            .progress(new ProgressTracker.AchievedRequirements())
            .icon(new CycleIcon(goatHorns.stream().map(ItemIcon::new).toArray(GoalIcon[]::new)))
            .antisynergy("goat_horn");
    }
}
