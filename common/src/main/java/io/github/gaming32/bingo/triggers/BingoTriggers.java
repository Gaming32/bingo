package io.github.gaming32.bingo.triggers;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

import static net.minecraft.advancements.CriteriaTriggers.register;

public class BingoTriggers {
    public static final ArrowPressTrigger ARROW_PRESS = register("bingo:arrow_press", new ArrowPressTrigger());
    public static final BeaconEffectTrigger BEACON_EFFECT = register("bingo:beacon_effect", new BeaconEffectTrigger());
    public static final BedRowTrigger BED_ROW = register("bingo:bed_row", new BedRowTrigger());
    public static final BounceOnBlockTrigger BOUNCE_ON_BLOCK = register("bingo:bounce_on_block", new BounceOnBlockTrigger());
    public static final BreakBlockTrigger BREAK_BLOCK = register("bingo:break_block", new BreakBlockTrigger());
    public static final ChickenHatchTrigger CHICKEN_HATCH = register("bingo:chicken_hatch", new ChickenHatchTrigger());
    public static final CompleteMapTrigger COMPLETED_MAP = register("bingo:completed_map", new CompleteMapTrigger());
    public static final DeathTrigger DEATH = register("bingo:death", new DeathTrigger());
    public static final DestroyVehicleTrigger DESTROY_VEHICLE = register("bingo:destroy_vehicle", new DestroyVehicleTrigger());
    public static final DifferentPotionsTrigger DIFFERENT_POTIONS = register("bingo:different_potions", new DifferentPotionsTrigger());
    public static final DistanceTrigger CROUCH = register("bingo:crouch", new DistanceTrigger());
    public static final EnchantedItemTrigger ENCHANTED_ITEM = register("bingo:enchanted_item", new EnchantedItemTrigger());
    public static final EntityDieNearPlayerTrigger ENTITY_DIE_NEAR_PLAYER = register("bingo:entity_die_near_player", new EntityDieNearPlayerTrigger());
    public static final EntityKilledPlayerTrigger ENTITY_KILLED_PLAYER = register("bingo:entity_killed_player", new EntityKilledPlayerTrigger());
    public static final EntityTrigger MOB_BROKE_CROSSBOW = register("bingo:mob_broke_crossbow", new EntityTrigger());
    public static final EntityTrigger STUN_RAVAGER = register("bingo:stun_ravager", new EntityTrigger());
    public static final EquipItemTrigger EQUIP_ITEM = register("bingo:equip_item", new EquipItemTrigger());
    public static final ExperienceChangeTrigger EXPERIENCE_CHANGED = register("bingo:experience_changed", new ExperienceChangeTrigger());
    public static final GrowBeeNestTreeTrigger GROW_BEE_NEST_TREE = register("bingo:grow_bee_nest_tree", new GrowBeeNestTreeTrigger());
    public static final GrowFeatureTrigger GROW_FEATURE = register("bingo:grow_feature", new GrowFeatureTrigger());
    public static final HasSomeFoodItemsTrigger HAS_SOME_FOOD_ITEMS = register("bingo:has_some_food_items", new HasSomeFoodItemsTrigger());
    public static final HasSomeItemsFromTagTrigger HAS_SOME_ITEMS_FROM_TAG = register("bingo:has_some_items_from_tag", new HasSomeItemsFromTagTrigger());
    public static final IntentionalGameDesignTrigger INTENTIONAL_GAME_DESIGN = register("bingo:intentional_game_design", new IntentionalGameDesignTrigger());
    public static final ItemBrokenTrigger ITEM_BROKEN = register("bingo:item_broken", new ItemBrokenTrigger());
    public static final ItemPickedUpTrigger ITEM_PICKED_UP = register("bingo:item_picked_up", new ItemPickedUpTrigger());
    public static final ItemUsedOnLocationTrigger SLEPT = register("bingo:slept", new ItemUsedOnLocationTrigger());
    public static final KeyPressedTrigger KEY_PRESSED = register("bingo:key_pressed", new KeyPressedTrigger());
    public static final KillItemTrigger KILL_ITEM = register("bingo:kill_item", new KillItemTrigger());
    public static final KillSelfTrigger KILL_SELF = register("bingo:kill_self", new KillSelfTrigger());
    public static final MineralPillarTrigger MINERAL_PILLAR = register("bingo:mineral_pillar", new MineralPillarTrigger());
    public static final PartyParrotsTrigger PARTY_PARROTS = register("bingo:party_parrots", new PartyParrotsTrigger());
    public static final PowerConduitTrigger POWER_CONDUIT = register("bingo:power_conduit", new PowerConduitTrigger());
    public static final RelativeStatsTrigger RELATIVE_STATS = register("bingo:relative_stats", new RelativeStatsTrigger());
    public static final TotalCountInventoryChangeTrigger TOTAL_COUNT_INVENTORY_CHANGED = register("bingo:total_count_inventory_changed", new TotalCountInventoryChangeTrigger());
    public static final TryUseItemTrigger TRY_USE_ITEM = register("bingo:try_use_item", new TryUseItemTrigger());
    public static final ZombifyPigTrigger ZOMBIFY_PIG = register("bingo:zombify_pig", new ZombifyPigTrigger());

    public static void load() {
    }

    public static Criterion<DistanceTrigger.TriggerInstance> crouch(DistancePredicate distance) {
        return CROUCH.createCriterion(
            new DistanceTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(distance))
        );
    }

    public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> slept(LootItemCondition... bedLocation) {
        return SLEPT.createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(
            Optional.empty(), Optional.of(ContextAwarePredicate.create(bedLocation))
        ));
    }
}
