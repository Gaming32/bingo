package io.github.gaming32.bingo.triggers;

import io.github.gaming32.bingo.ext.PlayerPredicateBuilderExt;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;

import static net.minecraft.advancements.CriteriaTriggers.register;

public class BingoTriggers {
    public static final ExperienceChangeTrigger EXPERIENCE_CHANGED = register(new ExperienceChangeTrigger());
    public static final EnchantedItemTrigger ENCHANTED_ITEM = register(new EnchantedItemTrigger());
    public static final ArrowPressTrigger ARROW_PRESS = register(new ArrowPressTrigger());
    public static final TryUseItemTrigger USE_ITEM = register(new TryUseItemTrigger());
    public static final EquipItemTrigger EQUIP_ITEM = register(new EquipItemTrigger());
    public static final DistanceTrigger CROUCH = register(new DistanceTrigger(new ResourceLocation("bingo:crouch")));
    public static final ItemBrokenTrigger ITEM_BROKEN = register(new ItemBrokenTrigger());
    public static final PlayerTrigger BOUNCE_ON_BED = register(new PlayerTrigger(new ResourceLocation("bingo:bounce_on_bed")));
    public static final CompleteMapTrigger COMPLETED_MAP = register(new CompleteMapTrigger());
    public static final BeaconEffectTrigger BEACON_EFFECT = register(new BeaconEffectTrigger());
    public static final TotalCountInventoryChangeTrigger TOTAL_COUNT_INVENTORY_CHANGE = register(new TotalCountInventoryChangeTrigger());
    public static final HasSomeItemsFromTagTrigger HAS_SOME_ITEMS_FROM_TAG = register(new HasSomeItemsFromTagTrigger());
    public static final BedRowTrigger BED_ROW = register(new BedRowTrigger());
    public static final MineralPillarTrigger MINERAL_PILLAR = register(new MineralPillarTrigger());
    public static final ItemPickedUpTrigger ITEM_PICKED_UP = register(new ItemPickedUpTrigger());
    public static final ZombifyPigTrigger ZOMBIFY_PIG = register(new ZombifyPigTrigger());
    public static final PartyParrotsTrigger PARTY_PARROTS = register(new PartyParrotsTrigger());
    public static final PowerConduitTrigger POWER_CONDUIT = register(new PowerConduitTrigger());
    public static final DifferentPotionsTrigger DIFFERENT_POTIONS = register(new DifferentPotionsTrigger());
    public static final KillSelfTrigger KILL_SELF = register(new KillSelfTrigger());
    public static final EntityDieNearPlayerTrigger ENTITY_DIE_NEAR_PLAYER = register(new EntityDieNearPlayerTrigger());
    public static final EntityKilledPlayerTrigger ENTITY_KILLED_PLAYER = register(new EntityKilledPlayerTrigger());
    public static final DeathTrigger DEATH = register(new DeathTrigger());
    public static final IntentionalGameDesignTrigger INTENTIONAL_GAME_DESIGN = register(new IntentionalGameDesignTrigger());
    public static final BreakBlockTrigger BREAK_BLOCK = register(new BreakBlockTrigger());

    public static void load() {
    }

    public static DistanceTrigger.TriggerInstance crouch(DistancePredicate distance) {
        return new DistanceTrigger.TriggerInstance(CROUCH.getId(), ContextAwarePredicate.ANY, LocationPredicate.ANY, distance);
    }

    public static PlayerTrigger.TriggerInstance bounceOnBed() {
        return new PlayerTrigger.TriggerInstance(BOUNCE_ON_BED.getId(), ContextAwarePredicate.ANY);
    }

    public static PlayerTrigger.TriggerInstance statChanged(Stat<?> relativeStat, MinMaxBounds.Ints toValue) {
        return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.getId(), EntityPredicate.wrap(
            EntityPredicate.Builder.entity().subPredicate(
                ((PlayerPredicateBuilderExt)PlayerPredicate.Builder.player())
                    .bingo$addRelativeStat(relativeStat, toValue)
                    .build()
            ).build()
        ));
    }
}
