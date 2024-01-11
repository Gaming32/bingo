package io.github.gaming32.bingo.triggers;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;
import java.util.function.Supplier;

public class BingoTriggers {
    private static final Registrar<CriterionTrigger<?>> REGISTRAR = Bingo.REGISTRAR_MANAGER.get(Registries.TRIGGER_TYPE);

    public static final RegistrySupplier<AdjacentPaintingTrigger> ADJACENT_PAINTING = register("adjacent_painting", AdjacentPaintingTrigger::new);
    public static final RegistrySupplier<ArrowPressTrigger> ARROW_PRESS = register("arrow_press", ArrowPressTrigger::new);
    public static final RegistrySupplier<BeaconEffectTrigger> BEACON_EFFECT = register("beacon_effect", BeaconEffectTrigger::new);
    public static final RegistrySupplier<BedRowTrigger> BED_ROW = register("bed_row", BedRowTrigger::new);
    public static final RegistrySupplier<BounceOnBlockTrigger> BOUNCE_ON_BLOCK = register("bounce_on_block", BounceOnBlockTrigger::new);
    public static final RegistrySupplier<BreakBlockTrigger> BREAK_BLOCK = register("break_block", BreakBlockTrigger::new);
    public static final RegistrySupplier<ChickenHatchTrigger> CHICKEN_HATCH = register("chicken_hatch", ChickenHatchTrigger::new);
    public static final RegistrySupplier<CompleteMapTrigger> COMPLETED_MAP = register("completed_map", CompleteMapTrigger::new);
    public static final RegistrySupplier<DeathTrigger> DEATH = register("death", DeathTrigger::new);
    public static final RegistrySupplier<DestroyVehicleTrigger> DESTROY_VEHICLE = register("destroy_vehicle", DestroyVehicleTrigger::new);
    public static final RegistrySupplier<DifferentColoredShields> DIFFERENT_COLORED_SHIELDS = register("different_colored_shields", DifferentColoredShields::new);
    public static final RegistrySupplier<DifferentPotionsTrigger> DIFFERENT_POTIONS = register("different_potions", DifferentPotionsTrigger::new);
    public static final RegistrySupplier<DistanceTrigger> CROUCH = register("crouch", DistanceTrigger::new);
    public static final RegistrySupplier<EnchantedItemTrigger> ENCHANTED_ITEM = register("enchanted_item", EnchantedItemTrigger::new);
    public static final RegistrySupplier<EntityDieNearPlayerTrigger> ENTITY_DIE_NEAR_PLAYER = register("entity_die_near_player", EntityDieNearPlayerTrigger::new);
    public static final RegistrySupplier<EntityKilledPlayerTrigger> ENTITY_KILLED_PLAYER = register("entity_killed_player", EntityKilledPlayerTrigger::new);
    public static final RegistrySupplier<EntityTrigger> MOB_BROKE_CROSSBOW = register("mob_broke_crossbow", EntityTrigger::new);
    public static final RegistrySupplier<EntityTrigger> STUN_RAVAGER = register("stun_ravager", EntityTrigger::new);
    public static final RegistrySupplier<EquipItemTrigger> EQUIP_ITEM = register("equip_item", EquipItemTrigger::new);
    public static final RegistrySupplier<ExperienceChangeTrigger> EXPERIENCE_CHANGED = register("experience_changed", ExperienceChangeTrigger::new);
    public static final RegistrySupplier<GrowBeeNestTreeTrigger> GROW_BEE_NEST_TREE = register("grow_bee_nest_tree", GrowBeeNestTreeTrigger::new);
    public static final RegistrySupplier<GrowFeatureTrigger> GROW_FEATURE = register("grow_feature", GrowFeatureTrigger::new);
    public static final RegistrySupplier<HasSomeFoodItemsTrigger> HAS_SOME_FOOD_ITEMS = register("has_some_food_items", HasSomeFoodItemsTrigger::new);
    public static final RegistrySupplier<HasSomeItemsFromTagTrigger> HAS_SOME_ITEMS_FROM_TAG = register("has_some_items_from_tag", HasSomeItemsFromTagTrigger::new);
    public static final RegistrySupplier<IntentionalGameDesignTrigger> INTENTIONAL_GAME_DESIGN = register("intentional_game_design", IntentionalGameDesignTrigger::new);
    public static final RegistrySupplier<ItemBrokenTrigger> ITEM_BROKEN = register("item_broken", ItemBrokenTrigger::new);
    public static final RegistrySupplier<ItemPickedUpTrigger> ITEM_PICKED_UP = register("item_picked_up", ItemPickedUpTrigger::new);
    public static final RegistrySupplier<ItemUsedOnLocationTrigger> SLEPT = register("slept", ItemUsedOnLocationTrigger::new);
    public static final RegistrySupplier<KeyPressedTrigger> KEY_PRESSED = register("key_pressed", KeyPressedTrigger::new);
    public static final RegistrySupplier<KillItemTrigger> KILL_ITEM = register("kill_item", KillItemTrigger::new);
    public static final RegistrySupplier<KillSelfTrigger> KILL_SELF = register("kill_self", KillSelfTrigger::new);
    public static final RegistrySupplier<MineralPillarTrigger> MINERAL_PILLAR = register("mineral_pillar", MineralPillarTrigger::new);
    public static final RegistrySupplier<PartyParrotsTrigger> PARTY_PARROTS = register("party_parrots", PartyParrotsTrigger::new);
    public static final RegistrySupplier<PowerConduitTrigger> POWER_CONDUIT = register("power_conduit", PowerConduitTrigger::new);
    public static final RegistrySupplier<RelativeStatsTrigger> RELATIVE_STATS = register("relative_stats", RelativeStatsTrigger::new);
    public static final RegistrySupplier<DoorOpenedByTargetTrigger> DOOR_OPENED_BY_TARGET = register("door_opened_by_target", DoorOpenedByTargetTrigger::new);
    public static final RegistrySupplier<TotalCountInventoryChangeTrigger> TOTAL_COUNT_INVENTORY_CHANGED = register("total_count_inventory_changed", TotalCountInventoryChangeTrigger::new);
    public static final RegistrySupplier<TryUseItemTrigger> TRY_USE_ITEM = register("try_use_item", TryUseItemTrigger::new);
    public static final RegistrySupplier<ZombieDrownedTrigger> ZOMBIE_DROWNED = register("zombie_drowned", ZombieDrownedTrigger::new);
    public static final RegistrySupplier<ZombifyPigTrigger> ZOMBIFY_PIG = register("zombify_pig", ZombifyPigTrigger::new);

    public static void load() {
    }

    private static <T extends CriterionTrigger<?>> RegistrySupplier<T> register(String name, Supplier<T> init) {
        return REGISTRAR.register(new ResourceLocation("bingo", name), init);
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
