package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.conditions.WearingDifferentArmorCondition;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.subs.CompoundBingoSub;
import io.github.gaming32.bingo.data.subs.SubBingoSub;
import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.triggers.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MediumGoalProvider extends DifficultyGoalProvider {
    public MediumGoalProvider(Consumer<BingoGoal> goalAdder) {
        super(2, "medium/", goalAdder);
    }

    @Override
    public void addGoals() {
        // TODO: different edible items
        addGoal(obtainItemGoal(id("beetroot_soup"), Items.BEETROOT_SOUP)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("potted_cactus"))
            .criterion("pot", CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(
                new ItemUsedOnLocationTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ContextAwarePredicate.create(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.POTTED_CACTUS).build()
                    ))
                ))
            )
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.potted_cactus"))
            .icon(Blocks.POTTED_CACTUS, Blocks.CACTUS)
        );
        // TODO: detonate TNT minecart
        addGoal(obtainItemGoal(id("magma_block"), Items.MAGMA_BLOCK, 10, 30));
        addGoal(obtainItemGoal(id("damaged_anvil"), Items.DAMAGED_ANVIL));
        addGoal(obtainItemGoal(id("melon_slice"), Items.MELON_SLICE, 16, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("never_wear_armor"))
            .criterion("equip", EquipItemTrigger.builder().newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR).build()).build())
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_wear_armor"))
            .icon(Items.DIAMOND_CHESTPLATE)
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor"));
        addGoal(BingoGoal.builder(id("skeleton_bow"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.BOW).build(),
                EntityPredicate.Builder.entity().of(EntityType.SKELETON).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.skeleton_bow"))
            .icon(Items.BOW));
        addGoal(obtainItemGoal(id("diamond_block"), Items.DIAMOND_BLOCK)
            .infrequency(2));
        addGoal(obtainItemGoal(id("lapis_block"), Items.LAPIS_BLOCK, 2, 3)
            .tags(BingoTags.OVERWORLD));
        // TODO: different saplings
        addGoal(BingoGoal.builder(id("tame_wolf"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.WOLF)))
            .tags(BingoTags.STAT, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.tame_wolf"))
            .icon(Items.BONE));
        addGoal(obtainItemGoal(id("fire_charge"), Items.FIRE_CHARGE, 6, 6)
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        addGoal(obtainItemGoal(id("magma_cream"), Items.MAGMA_CREAM, 2, 3)
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        // TODO: create iron golem
        addGoal(obtainItemGoal(id("ender_eye"), Items.ENDER_EYE)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        addGoal(obtainItemGoal(id("rabbit_stew"), Items.RABBIT_STEW)
            .tags(BingoTags.OVERWORLD));

        addGoal(potionGoal("fire_resistance_potion", Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE));
        addGoal(potionGoal("healing_potion", Potions.HEALING, Potions.STRONG_HEALING)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        addGoal(potionGoal("poison_potion", Potions.POISON, Potions.LONG_POISON, Potions.STRONG_POISON)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        addGoal(potionGoal("harming_potion", Potions.HARMING, Potions.STRONG_HARMING)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        addGoal(potionGoal("regeneration_potion", Potions.REGENERATION, Potions.LONG_REGENERATION, Potions.STRONG_REGENERATION)
            .tags(BingoTags.COMBAT));
        addGoal(potionGoal("slowness_potion", Potions.SLOWNESS, Potions.LONG_SLOWNESS, Potions.STRONG_SLOWNESS)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        addGoal(potionGoal("strength_potion", Potions.STRENGTH, Potions.LONG_STRENGTH, Potions.STRONG_STRENGTH)
            .tags(BingoTags.COMBAT));
        addGoal(potionGoal("swiftness_potion", Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        addGoal(potionGoal("weakness_potion", Potions.WEAKNESS, Potions.LONG_WEAKNESS)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        addGoal(potionGoal("leaping_potion", Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD, BingoTags.COMBAT));
        addGoal(potionGoal("slow_falling_potion", Potions.SLOW_FALLING, Potions.LONG_SLOW_FALLING)
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD));
        addGoal(potionGoal("turtle_master_potion", Potions.TURTLE_MASTER, Potions.LONG_TURTLE_MASTER, Potions.STRONG_TURTLE_MASTER)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD, BingoTags.COMBAT));

        addGoal(BingoGoal.builder(id("finish_by_free_falling"))
            .criterion("free_fall", DistanceTrigger.TriggerInstance.fallFromHeight(
                // Copy of Vanilla's "fall_from_world_height" or "Caves & Cliffs" advancement
                EntityPredicate.Builder.entity().located(
                    LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atMost(-59))
                ),
                DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(379)),
                LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319))
            ))
            .tags(BingoTags.ACTION, BingoTags.BUILD, BingoTags.OVERWORLD, BingoTags.FINISH)
            .name(Component.translatable("bingo.goal.finish_by_free_falling"))
            .tooltip(Component.translatable("advancements.adventure.fall_from_world_height.description"))
            .icon(Items.WATER_BUCKET)
        );
        // TODO: vegetarian
        addGoal(BingoGoal.builder(id("kill_self_with_arrow"))
            .criterion("kill", KillSelfTrigger.TriggerInstance.killSelf(
                DamageSourcePredicate.Builder.damageType()
                    .tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                    .direct(EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS))
                    .build()
            ))
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.kill_self_with_arrow"))
            .icon(Items.ARROW));
        addGoal(BingoGoal.builder(id("whilst_trying_to_escape"))
            .criterion("die", EntityKilledPlayerTrigger.builder()
                .sourceEntity(ContextAwarePredicate.create(
                    new LootItemEntityPropertyCondition(Optional.empty(), LootContext.EntityTarget.THIS),
                    InvertedLootItemCondition.invert(LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().build()
                    )).build()
                ))
                .build()
            )
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.whilst_trying_to_escape"))
            .tooltip(Component.translatable("bingo.goal.whilst_trying_to_escape.tooltip"))
            .icon(Items.ZOMBIE_HEAD)
        );
        addGoal(BingoGoal.builder(id("finish_on_top"))
            .criterion("on_top", PlayerTrigger.TriggerInstance.located(
                LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319))
            ))
            .tags(BingoTags.ACTION, BingoTags.BUILD, BingoTags.FINISH)
            .name(Component.translatable("bingo.goal.finish_on_top"))
            .tooltip(Component.translatable("bingo.goal.finish_on_top.tooltip"))
            .icon(new ItemStack(Items.DIRT, 320))
        );
        // TODO: kill hostile mob with gravel
        // TODO: kill hostile mob with sand
        // TODO: put carpet on llama
        addGoal(BingoGoal.builder(id("sized_nether_portal"))
            .sub("width", BingoSub.random(4, 6))
            .sub("height", BingoSub.random(4, 6))
            .criterion("activate",
                ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.NETHER_PORTAL),
                    BlockPatternCondition.builder().aisle("P")
                        .where('P', BlockPredicate.Builder.block().of(Blocks.NETHER_PORTAL))
                ),
                subber -> subber.sub("conditions.location.1.aisles.0", new CompoundBingoSub(
                    CompoundBingoSub.ElementType.ARRAY,
                    CompoundBingoSub.Operator.MUL,
                    BingoSub.wrapInArray(
                        new CompoundBingoSub(
                            CompoundBingoSub.ElementType.STRING,
                            CompoundBingoSub.Operator.MUL,
                            BingoSub.literal("P"),
                            new SubBingoSub("width")
                        )
                    ),
                    new SubBingoSub("height")
                ))
            )
            .tags(BingoTags.ACTION, BingoTags.BUILD, BingoTags.NETHER)
            .name(
                Component.translatable("bingo.goal.sized_nether_portal", 0, 0),
                subber -> subber.sub("with.0", "width").sub("with.1", "height")
            )
            .icon(Blocks.NETHER_PORTAL, Blocks.OBSIDIAN));
        addGoal(obtainItemGoal(id("obsidian"), Items.OBSIDIAN, 3, 10));
        addGoal(obtainItemGoal(id("iron_block"), Items.IRON_BLOCK, 5, 7)
            .infrequency(2));
        addGoal(obtainItemGoal(id("gold_block"), Items.GOLD_BLOCK, 2, 4)
            .infrequency(2));
        addGoal(obtainItemGoal(id("daylight_detector"), Items.DAYLIGHT_DETECTOR)
            .tags(BingoTags.OVERWORLD, BingoTags.NETHER));
        // TODO: enchanted golden sword
        // TODO: colors of wool
        // TODO: colors of terracotta
        // TODO: colors of glazed terracotta
        // TODO: colors of concrete
        addGoal(bedRowGoal(id("bed_row"), 7, 10));
        // TODO: power redstone lamp
        // TODO: different flowers
        // TODO: put zombified piglin in water
        addGoal(mineralPillarGoal(id("basic_mineral_blocks"), BingoBlockTags.BASIC_MINERAL_BLOCKS)
            .name(Component.translatable("bingo.goal.basic_mineral_blocks"))
            .tags(BingoTags.OVERWORLD)
            .icon(Blocks.DIAMOND_BLOCK)
        );
        // TODO: kill hostile mob with anvil
        addGoal(obtainLevelsGoal(id("levels"), 16, 26)
            .infrequency(2));
        // TODO: different seeds
        // TODO: 4 different armor types
        // TODO: fill hopper with 320 items
        addGoal(obtainItemGoal(id("red_nether_bricks"), Items.RED_NETHER_BRICKS, 16, 32)
            .reactant("use_furnace")
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("spectral_arrow"), Items.SPECTRAL_ARROW, 16, 32)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("rotten_flesh"), Items.ROTTEN_FLESH, 33, 64)
            .infrequency(2));
        addGoal(obtainItemGoal(id("ink_sac"), Items.INK_SAC, 16, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("slime_ball"), Items.SLIME_BALL, 5, 9)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        // TODO: lead on rabbit
        addGoal(obtainItemGoal(id("firework_star"), Items.FIREWORK_STAR)
            .tags(BingoTags.OVERWORLD));
        // TODO: hang mob with lead
        addGoal(obtainItemGoal(id("blaze_rod"), Items.BLAZE_ROD)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        addGoal(obtainItemGoal(id("ghast_tear"), Items.GHAST_TEAR)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        // TODO: never use coal
        addGoal(obtainItemGoal(id("glowstone_dust"), Items.GLOWSTONE_DUST, 32, 64)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("item_frame"), Items.ITEM_FRAME, 10, 32));
        addGoal(obtainSomeItemsFromTag(id("diamond_in_name"), Items.DIAMOND_HELMET, BingoItemTags.DIAMOND_IN_NAME, "bingo.goal.diamond_in_name", 3, 4)
            .antisynergy("diamond_items")
            .tooltip(Component.translatable("bingo.goal.diamond_in_name.tooltip")));
        addGoal(obtainItemGoal(id("prismarine_crystals"), Items.PRISMARINE_CRYSTALS, 2, 4)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        // TODO: dig straight down to bedrock from sea level (1x1 hole)
        // TODO: deplete diamond sword
        addGoal(obtainItemGoal(id("saddle"), Items.SADDLE));
        // TODO: give mob hat
        addGoal(obtainItemGoal(id("heart_of_the_sea"), Items.HEART_OF_THE_SEA)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("phantom_membrane"), Items.PHANTOM_MEMBRANE)
            .reactant("pacifist")
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD));
        // TODO: add marker to map
        // TODO: water, lava, milk, fish bucket
        // TODO: leash dolphin to fence
        addGoal(obtainItemGoal(id("dried_kelp_block"), Items.DRIED_KELP_BLOCK, 21, 32)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("gunpowder"), Items.GUNPOWDER, 6, 15)
            .infrequency(2)
            .tags(BingoTags.COMBAT));
        addGoal(obtainItemGoal(id("spider_eye"), Items.SPIDER_EYE, 6, 15)
            .infrequency(2)
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("ender_pearl"), Items.ENDER_PEARL, 4, 6)
            .infrequency(2)
            .tags(BingoTags.COMBAT));
        // TODO: never use axe
        // TODO: enchant an item
        // TODO: blue shield with white flower charge pattern
        addGoal(BingoGoal.builder(id("tame_cat"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.CAT)))
            .name(Component.translatable("bingo.goal.tame_cat"))
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD)
            .icon(Items.CAT_SPAWN_EGG));
        // TODO: breed mobs
        addGoal(crouchDistanceGoal(id("crouch_distance"), 200, 400));
        // TODO: kill n mobs
        addGoal(obtainItemGoal(id("seagrass"), Items.SEAGRASS, 33, 64)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        // TODO: kill iron golem
        // TODO: kill mob with end crystal
        addGoal(BingoGoal.builder(id("never_craft_sticks"))
            .criterion("craft", RecipeCraftedTrigger.TriggerInstance.craftedItem(new ResourceLocation("stick")))
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_craft_sticks"))
            .icon(Items.STICK));
        // TODO: light campfire from 10 blocks away
        // TODO: max scale map
        // TODO: ignite TNT with lectern
        // TODO: kill hostile mob with berry bush
        addGoal(BingoGoal.builder(id("pillager_crossbow"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.CROSSBOW).build(),
                EntityPredicate.Builder.entity().of(EntityType.PILLAGER).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name(Component.translatable("bingo.goal.pillager_crossbow"))
            .icon(Items.CROSSBOW));
        final ItemStack ominousBanner = simplifyBlockEntityStackData(Raid.getLeaderBannerInstance());
        addGoal(
            obtainItemGoal(
                id("ominous_banner"),
                ominousBanner,
                ItemPredicate.Builder.item().of(ominousBanner.getItem()).hasNbt(ominousBanner.getTag())
            )
            .antisynergy("ominous_banner")
            .name(Component.translatable("block.minecraft.ominous_banner"))
            .reactant("pacifist")
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD));
        // TODO: gain a fox's trust
        addGoal(obtainItemGoal(id("honey_block"), Items.HONEY_BLOCK)
            .setAntisynergy("honey")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("honeycomb_block"), Items.HONEYCOMB_BLOCK, 3, 3)
            .setAntisynergy("honeycomb")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        // TODO: repair iron golem
        // TODO: grow tree with benis attached

        for (String woodType : List.of("warped", "crimson")) {
            Item stemItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_stem"));
            addGoal(obtainItemGoal(id(woodType + "_stem"), stemItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));

            Item strippedStemItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_stem"));
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_stem"), strippedStemItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));

            Item hyphaeItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_hyphae"));
            addGoal(obtainItemGoal(id(woodType + "_hyphae"), hyphaeItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));

            Item strippedHyphaeItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_hyphae"));
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_hyphae"), strippedHyphaeItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));

            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            addGoal(obtainItemGoal(id(woodType + "_planks"), planksItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));
        }

        addGoal(obtainItemGoal(id("quartz_block"), Items.QUARTZ_BLOCK)
            .tags(BingoTags.NETHER));
        addGoal(BingoGoal.builder(id("anchor_in_overworld"))
            .criterion("anchor", IntentionalGameDesignTrigger.TriggerInstance.clicked(
                LocationPredicate.Builder.inDimension(Level.OVERWORLD).build()
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.anchor_in_overworld", Blocks.RESPAWN_ANCHOR.getName()))
            .icon(Blocks.RESPAWN_ANCHOR)
        );
        addGoal(obtainItemGoal(id("warped_fungus_on_a_stick"), Items.WARPED_FUNGUS_ON_A_STICK)
            .tags(BingoTags.NETHER));
        // TODO: convert hoglin into zoglin
        // TODO: ride strider
        // TODO: damage strider with water
        addGoal(obtainItemGoal(id("bamboo"), Items.BAMBOO, 6, 15)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(id("crying_obsidian"), Items.CRYING_OBSIDIAN));
        // TODO: kill self with ender pearl
        addGoal(obtainItemGoal(id("grass_block"), Items.GRASS_BLOCK)
            .tooltip(Component.translatable("bingo.goal.grass_block.tooltip"))
            .tags(BingoTags.OVERWORLD));
        // TODO: bounce on slime block
        // TODO: full gold armor
        addGoal(obtainItemGoal(id("brown_wool"), Items.BROWN_WOOL)
            .tags(BingoTags.OVERWORLD));
        // TODO: grow huge nether fungus
        // TODO: put chest on donkey
        addGoal(BingoGoal.builder(id("never_place_torches"))
            .criterion("place", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCH))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_place_torches"))
            .tooltip(Component.translatable("bingo.goal.never_place_torches.tooltip"))
            .icon(Items.TORCH));
        addGoal(obtainItemGoal(id("scute"), Items.SCUTE)
            .setAntisynergy("turtle_helmet")
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("all_different_armor"))
            .criterion("armor", CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
                new InventoryChangeTrigger.TriggerInstance(
                    Optional.of(
                        ContextAwarePredicate.create(new WearingDifferentArmorCondition(
                            MinMaxBounds.Ints.atLeast(4), MinMaxBounds.Ints.atLeast(4)
                        ))
                    ),
                    MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY,
                    List.of()
                )
            ))
            .tags(BingoTags.ITEM)
            .reactant("wear_armor")
            .name(Component.translatable("bingo.goal.all_different_armor", 4))
            .icon(makeItemWithGlint(Items.GOLDEN_CHESTPLATE))
        );
    }

    private BingoGoal.Builder potionGoal(String id, Potion... potions) {
        ItemStack potionItem = PotionUtils.setPotion(new ItemStack(Items.POTION), potions[0]);
        BingoGoal.Builder builder = obtainItemGoal(
            id(id),
            potionItem,
            Arrays.stream(potions).map(potion -> ItemPredicate.Builder.item().of(Items.POTION).isPotion(potion)).toArray(ItemPredicate.Builder[]::new))
            .antisynergy(id)
            .name(Items.POTION.getName(potionItem))
            .infrequency(12)
            .tags(BingoTags.NETHER);
        if (potions[0] != Potions.FIRE_RESISTANCE) {
            builder.reactant("pacifist");
        }
        return builder;
    }
}
