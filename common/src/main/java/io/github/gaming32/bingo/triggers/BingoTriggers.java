package io.github.gaming32.bingo.triggers;

import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;
import java.util.function.Supplier;

public class BingoTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> REGISTER =
        BingoPlatform.platform.createDeferredRegister(BuiltInRegistries.TRIGGER_TYPES);

    public static final RegistryValue<AdjacentPaintingTrigger> ADJACENT_PAINTING = register("adjacent_painting", AdjacentPaintingTrigger::new);
    public static final RegistryValue<ArrowPressTrigger> ARROW_PRESS = register("arrow_press", ArrowPressTrigger::new);
    public static final RegistryValue<BeaconEffectTrigger> BEACON_EFFECT = register("beacon_effect", BeaconEffectTrigger::new);
    public static final RegistryValue<BedRowTrigger> BED_ROW = register("bed_row", BedRowTrigger::new);
    public static final RegistryValue<BounceOnBlockTrigger> BOUNCE_ON_BLOCK = register("bounce_on_block", BounceOnBlockTrigger::new);
    public static final RegistryValue<BreakBlockTrigger> BREAK_BLOCK = register("break_block", BreakBlockTrigger::new);
    public static final RegistryValue<ChickenHatchTrigger> CHICKEN_HATCH = register("chicken_hatch", ChickenHatchTrigger::new);
    public static final RegistryValue<CompleteMapTrigger> COMPLETED_MAP = register("completed_map", CompleteMapTrigger::new);
    public static final RegistryValue<DistanceTrigger> CROUCH = register("crouch", DistanceTrigger::new);
    public static final RegistryValue<DeathTrigger> DEATH = register("death", DeathTrigger::new);
    public static final RegistryValue<DestroyVehicleTrigger> DESTROY_VEHICLE = register("destroy_vehicle", DestroyVehicleTrigger::new);
    public static final RegistryValue<DifferentColoredShieldsTrigger> DIFFERENT_COLORED_SHIELDS = register("different_colored_shields", DifferentColoredShieldsTrigger::new);
    public static final RegistryValue<DifferentPotionsTrigger> DIFFERENT_POTIONS = register("different_potions", DifferentPotionsTrigger::new);
    public static final RegistryValue<DoorOpenedByTargetTrigger> DOOR_OPENED_BY_TARGET = register("door_opened_by_target", DoorOpenedByTargetTrigger::new);
    public static final RegistryValue<EnchantedItemTrigger> ENCHANTED_ITEM = register("enchanted_item", EnchantedItemTrigger::new);
    public static final RegistryValue<EntityDieNearPlayerTrigger> ENTITY_DIE_NEAR_PLAYER = register("entity_die_near_player", EntityDieNearPlayerTrigger::new);
    public static final RegistryValue<EntityKilledPlayerTrigger> ENTITY_KILLED_PLAYER = register("entity_killed_player", EntityKilledPlayerTrigger::new);
    public static final RegistryValue<EquipItemTrigger> EQUIP_ITEM = register("equip_item", EquipItemTrigger::new);
    public static final RegistryValue<ExperienceChangeTrigger> EXPERIENCE_CHANGED = register("experience_changed", ExperienceChangeTrigger::new);
    public static final RegistryValue<ExplosionTrigger> EXPLOSION = register("explosion", ExplosionTrigger::new);
    public static final RegistryValue<FillBundleTrigger> FILL_BUNDLE = register("fill_bundle", FillBundleTrigger::new);
    public static final RegistryValue<GrowBeeNestTreeTrigger> GROW_BEE_NEST_TREE = register("grow_bee_nest_tree", GrowBeeNestTreeTrigger::new);
    public static final RegistryValue<GrowFeatureTrigger> GROW_FEATURE = register("grow_feature", GrowFeatureTrigger::new);
    public static final RegistryValue<HasSomeFoodItemsTrigger> HAS_SOME_FOOD_ITEMS = register("has_some_food_items", HasSomeFoodItemsTrigger::new);
    public static final RegistryValue<HasSomeItemsFromTagTrigger> HAS_SOME_ITEMS_FROM_TAG = register("has_some_items_from_tag", HasSomeItemsFromTagTrigger::new);
    public static final RegistryValue<IntentionalGameDesignTrigger> INTENTIONAL_GAME_DESIGN = register("intentional_game_design", IntentionalGameDesignTrigger::new);
    public static final RegistryValue<ItemPickedUpTrigger> ITEM_PICKED_UP = register("item_picked_up", ItemPickedUpTrigger::new);
    public static final RegistryValue<KeyPressedTrigger> KEY_PRESSED = register("key_pressed", KeyPressedTrigger::new);
    public static final RegistryValue<KillItemTrigger> KILL_ITEM = register("kill_item", KillItemTrigger::new);
    public static final RegistryValue<KillSelfTrigger> KILL_SELF = register("kill_self", KillSelfTrigger::new);
    public static final RegistryValue<LeashedEntityTrigger> LEASHED_ENTITY = register("leash_entity", LeashedEntityTrigger::new);
    public static final RegistryValue<MineralPillarTrigger> MINERAL_PILLAR = register("mineral_pillar", MineralPillarTrigger::new);
    public static final RegistryValue<EntityTrigger> MOB_BROKE_CROSSBOW = register("mob_broke_crossbow", EntityTrigger::new);
    public static final RegistryValue<PartyParrotsTrigger> PARTY_PARROTS = register("party_parrots", PartyParrotsTrigger::new);
    public static final RegistryValue<PowerConduitTrigger> POWER_CONDUIT = register("power_conduit", PowerConduitTrigger::new);
    public static final RegistryValue<PulledByLeashTrigger> PULLED_BY_LEASH = register("pulled_by_leash", PulledByLeashTrigger::new);
    public static final RegistryValue<RelativeStatsTrigger> RELATIVE_STATS = register("relative_stats", RelativeStatsTrigger::new);
    public static final RegistryValue<ShootBellTrigger> SHOOT_BELL = register("bell_ring", ShootBellTrigger::new);
    public static final RegistryValue<ItemUsedOnLocationTrigger> SLEPT = register("slept", ItemUsedOnLocationTrigger::new);
    public static final RegistryValue<EntityTrigger> STUN_RAVAGER = register("stun_ravager", EntityTrigger::new);
    public static final RegistryValue<TotalCountInventoryChangeTrigger> TOTAL_COUNT_INVENTORY_CHANGED = register("total_count_inventory_changed", TotalCountInventoryChangeTrigger::new);
    public static final RegistryValue<TryUseItemTrigger> TRY_USE_ITEM = register("try_use_item", TryUseItemTrigger::new);
    public static final RegistryValue<UseGrindstoneTrigger> USE_GRINDSTONE = register("use_grindstone", UseGrindstoneTrigger::new);
    public static final RegistryValue<WearDifferentColoredArmorTrigger> WEAR_DIFFERENT_COLORED_ARMOR = register("wear_different_colored_armor", WearDifferentColoredArmorTrigger::new);
    public static final RegistryValue<ZombieDrownedTrigger> ZOMBIE_DROWNED = register("zombie_drowned", ZombieDrownedTrigger::new);
    public static final RegistryValue<ZombifyPigTrigger> ZOMBIFY_PIG = register("zombify_pig", ZombifyPigTrigger::new);

    public static void load() {
    }

    private static <T extends CriterionTrigger<?>> RegistryValue<T> register(String name, Supplier<T> init) {
        return REGISTER.register(ResourceLocations.bingo(name), init);
    }

    public static Criterion<DistanceTrigger.TriggerInstance> crouch(DistancePredicate distance) {
        return CROUCH.get().createCriterion(
            new DistanceTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(distance))
        );
    }

    public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> slept(LootItemCondition... bedLocation) {
        return SLEPT.get().createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(
            Optional.empty(), Optional.of(ContextAwarePredicate.create(bedLocation))
        ));
    }
}
