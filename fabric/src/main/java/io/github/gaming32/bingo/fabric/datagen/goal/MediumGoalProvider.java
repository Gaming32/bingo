package io.github.gaming32.bingo.fabric.datagen.goal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.conditions.OneByOneHoleCondition;
import io.github.gaming32.bingo.conditions.WearingDifferentArmorCondition;
import io.github.gaming32.bingo.data.BingoDifficulties;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.EntityIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.icons.IndicatorIcon;
import io.github.gaming32.bingo.data.icons.ItemIcon;
import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.subs.CompoundBingoSub;
import io.github.gaming32.bingo.data.subs.SubBingoSub;
import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.BingoDamageTypeTags;
import io.github.gaming32.bingo.data.tags.BingoEntityTypeTags;
import io.github.gaming32.bingo.data.tags.BingoFeatureTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.triggers.BounceOnBlockTrigger;
import io.github.gaming32.bingo.triggers.EntityDieNearPlayerTrigger;
import io.github.gaming32.bingo.triggers.EntityKilledPlayerTrigger;
import io.github.gaming32.bingo.triggers.EquipItemTrigger;
import io.github.gaming32.bingo.triggers.ExplosionTrigger;
import io.github.gaming32.bingo.triggers.GrowBeeNestTreeTrigger;
import io.github.gaming32.bingo.triggers.GrowFeatureTrigger;
import io.github.gaming32.bingo.triggers.IntentionalGameDesignTrigger;
import io.github.gaming32.bingo.triggers.ItemPickedUpTrigger;
import io.github.gaming32.bingo.triggers.KillSelfTrigger;
import io.github.gaming32.bingo.triggers.PulledByLeashTrigger;
import io.github.gaming32.bingo.triggers.RelativeStatsTrigger;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class MediumGoalProvider extends DifficultyGoalProvider {
    public MediumGoalProvider(Consumer<BingoGoal.Holder> goalAdder) {
        super(BingoDifficulties.MEDIUM, goalAdder);
    }

    @Override
    public void addGoals() {
        addGoal(obtainSomeEdibleItems(id("edible_items"), 6, 7).tags(BingoTags.OVERWORLD));
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
            .name("potted_cactus")
            .icon(Blocks.POTTED_CACTUS, Blocks.CACTUS)
        );
        addGoal(BingoGoal.builder(id("shoot_tnt_minecart"))
            .criterion("explode", ExplosionTrigger.builder()
                .source(EntityPredicate.Builder.entity().of(EntityType.TNT_MINECART))
                .build()
            )
            .name(Component.translatable("bingo.goal.shoot_tnt_minecart", EntityType.TNT_MINECART.getDescription()))
            .icon(CycleIcon.infer(makeItemWithGlint(Items.BOW), Items.ARROW, Items.TNT_MINECART))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        addGoal(obtainItemGoal(id("magma_block"), Items.MAGMA_BLOCK, 10, 30));
        addGoal(obtainItemGoal(id("damaged_anvil"), Items.DAMAGED_ANVIL));
        addGoal(obtainItemGoal(id("melon_slice"), Items.MELON_SLICE, 16, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("never_wear_armor"))
            .criterion("equip", EquipItemTrigger.builder()
                .newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR).build())
                .slots(EquipmentSlot.Type.ARMOR)
                .build()
            )
            .tags(BingoTags.NEVER)
            .name("never_wear_armor")
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
            .name("skeleton_bow")
            .icon(Items.BOW));
        addGoal(obtainItemGoal(id("diamond_block"), Items.DIAMOND_BLOCK)
            .infrequency(2));
        addGoal(obtainItemGoal(id("lapis_block"), Items.LAPIS_BLOCK, 2, 3)
            .tags(BingoTags.OVERWORLD));
        addGoal(
            obtainSomeItemsFromTag(
                id("different_saplings"),
                ItemTags.SAPLINGS,
                "bingo.goal.different_saplings",
                3, 5
            )
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(id("tame_wolf"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.WOLF)))
            .tags(BingoTags.STAT, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name("tame_wolf")
            .icon(Items.BONE));
        addGoal(obtainItemGoal(id("fire_charge"), Items.FIRE_CHARGE, 6, 6)
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        addGoal(obtainItemGoal(id("magma_cream"), Items.MAGMA_CREAM, 2, 3)
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        addGoal(BingoGoal.builder(id("create_iron_golem"))
            .criterion("summon", SummonedEntityTrigger.TriggerInstance.summonedEntity(
                EntityPredicate.Builder.entity().of(EntityType.IRON_GOLEM)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.create_iron_golem", EntityType.IRON_GOLEM.getDescription()))
            .icon(EntityIcon.ofSpawnEgg(EntityType.IRON_GOLEM))
        );
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
            .name("finish_by_free_falling")
            .tooltip(Component.translatable("advancements.adventure.fall_from_world_height.description"))
            .icon(Items.WATER_BUCKET)
        );
        addGoal(BingoGoal.builder(id("vegetarian"))
            .criterion("meat", ConsumeItemTrigger.TriggerInstance.usedItem(
                ItemPredicate.Builder.item()
                    .of(BingoItemTags.MEAT)
            ))
            .tags(BingoTags.NEVER, BingoTags.ACTION)
            .antisynergy("food")
            .catalyst("eat_meat")
            .name("vegetarian")
            .tooltip("vegetarian")
            .icon(new ItemTagCycleIcon(BingoItemTags.NOT_MEAT))
        );
        addGoal(BingoGoal.builder(id("kill_self_with_arrow"))
            .criterion("kill", KillSelfTrigger.TriggerInstance.killSelf(
                DamageSourcePredicate.Builder.damageType()
                    .tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                    .direct(EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS))
                    .build()
            ))
            .tags(BingoTags.ACTION)
            .name("kill_self_with_arrow")
            .icon(Items.ARROW));
        addGoal(BingoGoal.builder(id("while_trying_to_escape"))
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
            .name("while_trying_to_escape")
            .tooltip("while_trying_to_escape")
            .icon(Items.ZOMBIE_HEAD)
        );
        addGoal(BingoGoal.builder(id("finish_on_top"))
            .criterion("on_top", PlayerTrigger.TriggerInstance.located(
                LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319))
            ))
            .tags(BingoTags.ACTION, BingoTags.BUILD, BingoTags.FINISH)
            .name("finish_on_top")
            .tooltip("finish_on_top")
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
        addGoal(
            obtainItemGoal(
                id("enchanted_golden_sword"),
                new ItemIcon(makeItemWithGlint(Items.GOLDEN_SWORD)),
                ItemPredicate.Builder.item()
                    .of(Items.GOLDEN_SWORD)
                    .hasEnchantment(new EnchantmentPredicate(Optional.empty(), MinMaxBounds.Ints.atLeast(1)))
            )
                .name("enchanted_golden_sword")
        );
        addGoal(obtainSomeItemsFromTag(id("wool_colors"), ItemTags.WOOL, "bingo.goal.wool_colors", 8, 11)
            .antisynergy("wool_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("terracotta"), ItemTags.TERRACOTTA, "bingo.goal.colors_of_terracotta", 8, 11)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD)
            .reactant("use_furnace")
            .antisynergy("terracotta_color")
            .infrequency(4)
        );
        addGoal(obtainSomeItemsFromTag(id("glazed_terracotta"), BingoItemTags.GLAZED_TERRACOTTA, "bingo.goal.glazed_terracotta", 7, 10)
            .reactant("use_furnace")
            .antisynergy("glazed_terracotta_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("concrete"), BingoItemTags.CONCRETE, "bingo.goal.concrete", 7, 10)
            .antisynergy("concrete_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(bedRowGoal(id("bed_row"), 7, 10));
        // TODO: power redstone lamp
        addGoal(obtainSomeItemsFromTag(id("different_flowers"), ItemTags.FLOWERS, "bingo.goal.different_flowers", 8, 10)
            .antisynergy("flowers")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        // TODO: put zombified piglin in water
        addGoal(mineralPillarGoal(id("basic_mineral_blocks"), BingoBlockTags.BASIC_MINERAL_BLOCKS)
            .name("basic_mineral_blocks")
            .tags(BingoTags.OVERWORLD)
            .icon(new ItemTagCycleIcon(BingoItemTags.BASIC_MINERAL_BLOCKS))
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
        addGoal(BingoGoal.builder(id("lead_on_rabbit"))
            .criterion("use", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.LEAD),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.RABBIT)))
            ))
            .name(Component.translatable(
                "bingo.goal.lead_on_rabbit",
                Items.LEAD.getDescription(), EntityType.RABBIT.getDescription()
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .icon(Items.LEAD)
        );
        addGoal(obtainItemGoal(id("firework_star"), Items.FIREWORK_STAR)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("hang_mob"))
            .criterion("hang", PulledByLeashTrigger.builder()
                .knotRequired(true)
                .force(DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(0.1)))
                .build()
            )
            .name("hang_mob")
            .tooltip("hang_mob")
            .icon(CycleIcon.infer(EntityType.PIG, Items.LEAD, Items.OAK_FENCE))
            .reactant("pacifist")
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
        );
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
        addGoal(obtainSomeItemsFromTag(id("diamond_in_name"), BingoItemTags.DIAMOND_IN_NAME, "bingo.goal.diamond_in_name", 3, 4)
            .antisynergy("diamond_items")
            .tooltip("diamond_in_name"));
        addGoal(obtainItemGoal(id("prismarine_crystals"), Items.PRISMARINE_CRYSTALS, 2, 4)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("one_by_one_hole"))
            .criterion("hole", CriteriaTriggers.LOCATION.createCriterion(
                new PlayerTrigger.TriggerInstance(Optional.of(
                    ContextAwarePredicate.create(new OneByOneHoleCondition(
                        -59, 63,
                        net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate.not(
                            net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate.solid()
                        )
                    ))
                ))
            ))
            .name("one_by_one_hole")
            .tooltip("one_by_one_hole")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(Items.DIAMOND_PICKAXE)
        );
        // TODO: deplete diamond sword
        addGoal(obtainItemGoal(id("saddle"), Items.SADDLE));
        // TODO: give mob hat
        addGoal(obtainItemGoal(id("heart_of_the_sea"), Items.HEART_OF_THE_SEA)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("phantom_membrane"), Items.PHANTOM_MEMBRANE)
            .reactant("pacifist")
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("marker_on_map"))
            .criterion("use", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location(),
                ItemPredicate.Builder.item()
                    .of(Items.FILLED_MAP)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("marker_on_map")
            .icon(Items.BLUE_BANNER)
        );
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
        addGoal(BingoGoal.builder(id("enchant_item"))
            .criterion("enchant", EnchantedItemTrigger.TriggerInstance.enchantedItem())
            .name("enchant_item")
            .icon(makeItemWithGlint(Items.IRON_SWORD))
            .antisynergy("enchant")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        {
            final ItemStack shieldItem = new ItemStack(Items.SHIELD);
            final ListTag patternsTag = new BannerPattern.Builder()
                .addPattern(BannerPatterns.FLOWER, DyeColor.WHITE)
                .toListTag();
            BlockItem.setBlockEntityData(
                shieldItem, BlockEntityType.BANNER,
                BingoUtil.compound(Map.of(
                    "Patterns", patternsTag,
                    "Base", IntTag.valueOf(11)
                ))
            );
            assert shieldItem.getTag() != null;
            final CompoundTag itemNbt = shieldItem.getTag().copy();
            itemNbt.remove("Damage");
            addGoal(
                obtainItemGoal(
                    id("blue_shield_with_white_flower_charge"), shieldItem,
                    ItemPredicate.Builder.item()
                        .of(Items.SHIELD)
                        .hasNbt(itemNbt)
                ).name(Component.translatable(
                    "bingo.goal.item_with_pattern",
                    Component.translatable(Items.SHIELD.getDescriptionId() + "." + DyeColor.BLUE.getName()),
                    Component.translatable(
                        "block.minecraft.banner." +
                            BannerPatterns.FLOWER.location().toShortLanguageKey() + "." +
                            DyeColor.WHITE.getName()
                    )
                ))
                .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            );
        }
        addGoal(BingoGoal.builder(id("tame_cat"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.CAT)))
            .name("tame_cat")
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD)
            .icon(EntityIcon.ofSpawnEgg(EntityType.CAT))
        );
        addGoal(BingoGoal.builder(id("breed_mobs"))
            .sub("count", BingoSub.random(5, 7))
            .criterion(
                "breed",
                RelativeStatsTrigger.builder()
                    .stat(Stats.ANIMALS_BRED, MinMaxBounds.Ints.atLeast(0))
                    .build(),
                subber -> subber.sub("conditions.stats.0.value.min", "count")
            )
            .progress("breed")
            .name(
                Component.translatable("bingo.goal.breed_mobs", 0),
                subber -> subber.sub("with.0", "count")
            )
            .tooltip("breed_mobs")
            .icon(Items.WHEAT_SEEDS)
            .antisynergy("breed_animals")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
        );
        addGoal(crouchDistanceGoal(id("crouch_distance"), 200, 400));
        addGoal(BingoGoal.builder(id("kill_mobs"))
            .sub("count", BingoSub.random(75, 100))
            .criterion(
                "kill",
                RelativeStatsTrigger.builder()
                    .stat(Stats.MOB_KILLS, MinMaxBounds.Ints.atLeast(0))
                    .build(),
                subber -> subber.sub("conditions.stats.0.value.min", "count")
            )
            .progress("kill")
            .name(
                Component.translatable("bingo.goal.kill_mobs", 0),
                subber -> subber.sub("with.0", "count")
            )
            .icon(IndicatorIcon.infer(
                new CycleIcon(
                    BuiltInRegistries.ENTITY_TYPE.stream()
                        .filter(t -> t.getCategory() != MobCategory.MISC)
                        .filter(t -> SpawnEggItem.byId(t) != null)
                        .map(EntityIcon::ofSpawnEgg)
                        .map(i -> (GoalIcon)i)
                        .collect(ImmutableList.toImmutableList())
                ),
                Items.DIAMOND_SWORD
            ))
            .reactant("pacifist")
            .antisynergy("mob_kills")
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.STAT)
        );
        addGoal(obtainItemGoal(id("seagrass"), Items.SEAGRASS, 33, 64)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("kill_golem"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity(
                EntityPredicate.Builder.entity().of(EntityType.IRON_GOLEM)
            ))
            .name(Component.translatable("bingo.goal.kill_golem", EntityType.IRON_GOLEM.getDescription()))
            .icon(IndicatorIcon.infer(EntityType.IRON_GOLEM, Items.DIAMOND_SWORD))
            .reactant("pacifist")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(id("kill_with_crystal"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity(
                Optional.empty(),
                DamageSourcePredicate.Builder.damageType()
                    .direct(EntityPredicate.Builder.entity().of(EntityType.END_CRYSTAL))
            ))
            .name(Component.translatable("bingo.goal.kill_with_crystal", EntityType.END_CRYSTAL.getDescription()))
            .icon(Items.END_CRYSTAL)
            .reactant("pacifist")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.NETHER, BingoTags.COMBAT)
        );
        addGoal(BingoGoal.builder(id("never_craft_sticks"))
            .criterion("craft", RecipeCraftedTrigger.TriggerInstance.craftedItem(new ResourceLocation("stick")))
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name("never_craft_sticks")
            .icon(Items.STICK));
        // TODO: light campfire from 10 blocks away
        // TODO: max scale map
        // TODO: ignite TNT with lectern
        addGoal(BingoGoal.builder(id("kill_hostile_with_berries"))
            .criterion("kill", EntityDieNearPlayerTrigger.builder()
//                .entity(ContextAwarePredicate.create(
//                    new LootItemEntityPropertyCondition(
//                        Optional.of(EntityPredicate.Builder.entity()
//                            .of(BingoEntityTypeTags.HOSTILE)
//                            .build()
//                        ),
//                        LootContext.EntityTarget.THIS
//                    ),
//                    HasOnlyBeenDamagedByCondition.builder()
//                        .damageTypeTag(BingoDamageTypeTags.BERRY_BUSH)
//                        .build()
//                ))
                .entity(EntityPredicate.Builder.entity()
                    .of(BingoEntityTypeTags.HOSTILE)
                    .build()
                )
                .killingBlow(DamagePredicate.Builder.damageInstance()
                    .type(DamageSourcePredicate.Builder.damageType()
                        .tag(TagPredicate.is(BingoDamageTypeTags.BERRY_BUSH))
                    )
                    .build()
                )
                .build()
            )
            .name(Component.translatable("bingo.goal.kill_hostile_with_berries", Blocks.SWEET_BERRY_BUSH.getName()))
            .icon(IndicatorIcon.infer(BingoEntityTypeTags.HOSTILE, Items.SWEET_BERRIES))
            .reactant("pacifist")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.COMBAT, BingoTags.RARE_BIOME)
        );
        addGoal(BingoGoal.builder(id("pillager_crossbow"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.CROSSBOW).build(),
                EntityPredicate.Builder.entity().of(EntityType.PILLAGER).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name("pillager_crossbow")
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
        addGoal(BingoGoal.builder(id("gain_fox_trust"))
            .criterion("breed", BredAnimalsTrigger.TriggerInstance.bredAnimals(
                EntityPredicate.Builder.entity().of(EntityType.FOX)
            ))
            .name(Component.translatable("bingo.goal.gain_fox_trust", EntityType.FOX.getDescription()))
            .icon(EntityIcon.ofSpawnEgg(EntityType.FOX))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
        );
        addGoal(obtainItemGoal(id("honey_block"), Items.HONEY_BLOCK)
            .setAntisynergy("honey")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("honeycomb_block"), Items.HONEYCOMB_BLOCK, 3, 3)
            .setAntisynergy("honeycomb")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("repair_iron_golem"))
            .criterion("repair", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.IRON_INGOT),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.IRON_GOLEM)))
            ))
            .name(Component.translatable("bingo.goal.repair_iron_golem", EntityType.IRON_GOLEM.getDescription()))
            .icon(EntityIcon.ofSpawnEgg(EntityType.IRON_GOLEM, BingoUtil.compound(Map.of(
                "Health", FloatTag.valueOf(40f)
            ))))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.VILLAGE)
        );
        addGoal(BingoGoal.builder(id("grow_tree_with_bee_nest"))
            .criterion("grow", GrowBeeNestTreeTrigger.TriggerInstance.grew())
            .name(Component.translatable("bingo.goal.grow_tree_with_bee_nest", Blocks.BEE_NEST.getName()))
            .icon(Items.BEE_NEST)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
        );

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
        addGoal(BingoGoal.builder(id("ride_strider"))
            .criterion("ride", StartRidingTrigger.TriggerInstance.playerStartsRiding(
                EntityPredicate.Builder.entity().vehicle(EntityPredicate.Builder.entity().of(EntityType.STRIDER))
            ))
            .name(Component.translatable("bingo.goal.ride_strider", EntityType.STRIDER.getDescription()))
            .icon(EntityIcon.ofSpawnEgg(EntityType.STRIDER))
            .tags(BingoTags.ACTION, BingoTags.NETHER)
        );
        // TODO: damage strider with water
        addGoal(obtainItemGoal(id("bamboo"), Items.BAMBOO, 6, 15)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(id("crying_obsidian"), Items.CRYING_OBSIDIAN));
        // TODO: kill self with ender pearl
        addGoal(obtainItemGoal(id("grass_block"), Items.GRASS_BLOCK)
            .tooltip("grass_block")
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("bounce_on_slime"))
            .criterion("bounce", BounceOnBlockTrigger.TriggerInstance.bounceOnBlock(
                BlockPredicate.Builder.block().of(Blocks.SLIME_BLOCK)
            ))
            .name(Component.translatable("bingo.goal.bounce_on_slime", Blocks.SLIME_BLOCK.getName()))
            .icon(Items.SLIME_BLOCK)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(id("full_gold_armor"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_HELMET
            ))
            .name("full_gold_armor")
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.GOLDEN_BOOTS),
                ItemIcon.ofItem(Items.GOLDEN_LEGGINGS),
                ItemIcon.ofItem(Items.GOLDEN_CHESTPLATE),
                ItemIcon.ofItem(Items.GOLDEN_HELMET)
            ))
            .tags(BingoTags.ITEM)
        );
        addGoal(obtainItemGoal(id("brown_wool"), Items.BROWN_WOOL)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("grow_nether_fungus"))
            .criterion("grow", GrowFeatureTrigger.builder().feature(BingoFeatureTags.HUGE_FUNGI).build())
            .name("grow_nether_fungus")
            .icon(new CycleIcon(ItemIcon.ofItem(Items.CRIMSON_FUNGUS), ItemIcon.ofItem(Items.WARPED_FUNGUS)))
            .antisynergy("grow_fungus")
            .tags(BingoTags.ACTION, BingoTags.NETHER)
        );
        // TODO: put chest on donkey
        addGoal(BingoGoal.builder(id("never_place_torches"))
            .criterion("place", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCH))
            .tags(BingoTags.NEVER)
            .name("never_place_torches")
            .tooltip("never_place_torches")
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
                    InventoryChangeTrigger.TriggerInstance.Slots.ANY,
                    List.of()
                )
            ))
            .tags(BingoTags.ITEM)
            .reactant("wear_armor")
            .name(Component.translatable("bingo.goal.all_different_armor", 4))
            .icon(createAllDifferentMaterialsIcon())
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

    private static GoalIcon createAllDifferentMaterialsIcon() {
        final int iterations = 4;
        final var armors = getArmors();
        final List<ArmorMaterials> materials = ImmutableList.copyOf(armors.columnKeySet());
        final ImmutableList.Builder<GoalIcon> icons = ImmutableList.builderWithExpectedSize(iterations * armors.rowMap().size());
        int materialIndex = 0;
        for (int iteration = 0; iteration < iterations; iteration++) {
            for (final ArmorItem.Type type : armors.rowKeySet()) {
                ArmorItem item;
                do {
                    item = armors.get(type, materials.get(materialIndex++ % materials.size()));
                } while (item == null);
                icons.add(ItemIcon.ofItem(item));
            }
        }
        return new CycleIcon(icons.build());
    }

    private static Table<ArmorItem.Type, ArmorMaterials, ArmorItem> getArmors() {
        final ImmutableTable.Builder<ArmorItem.Type, ArmorMaterials, ArmorItem> armors = ImmutableTable.builder();
        armors.orderRowsBy(Comparator.reverseOrder());
        armors.orderColumnsBy(Comparator.naturalOrder());
        for (final Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof ArmorItem armorItem)) continue;
            if (!(armorItem.getMaterial() instanceof ArmorMaterials vanillaMaterial)) continue;
            armors.put(armorItem.getType(), vanillaMaterial, armorItem);
        }
        return armors.build();
    }
}
