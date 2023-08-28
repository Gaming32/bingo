package io.github.gaming32.bingo.triggers;

import static net.minecraft.advancements.CriteriaTriggers.register;

public class BingoTriggers {
    public static final ExperienceChangeTrigger EXPERIENCE_CHANGED = register(new ExperienceChangeTrigger());

    public static void load() {
    }
}
