package io.github.gaming32.bingo.triggers;

import static net.minecraft.advancements.CriteriaTriggers.register;

public class BingoTriggers {
    public static final ExperienceChangeTrigger EXPERIENCE_CHANGED = register(new ExperienceChangeTrigger());
    public static final EnchantedItemTrigger ENCHANTED_ITEM = register(new EnchantedItemTrigger());
    public static final ArrowPressTrigger ARROW_PRESS = register(new ArrowPressTrigger());
    public static final TryUseItemTrigger USE_ITEM = register(new TryUseItemTrigger());
    public static final EquipItemTrigger EQUIP_ITEM = register(new EquipItemTrigger());

    public static void load() {
    }
}
