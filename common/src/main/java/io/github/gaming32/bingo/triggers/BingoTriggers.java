package io.github.gaming32.bingo.triggers;

import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;

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

    public static void load() {
    }

    public static DistanceTrigger.TriggerInstance crouch(DistancePredicate distance) {
        return new DistanceTrigger.TriggerInstance(CROUCH.getId(), ContextAwarePredicate.ANY, LocationPredicate.ANY, distance);
    }

    public static PlayerTrigger.TriggerInstance bounceOnBed() {
        return new PlayerTrigger.TriggerInstance(BOUNCE_ON_BED.getId(), ContextAwarePredicate.ANY);
    }
}
