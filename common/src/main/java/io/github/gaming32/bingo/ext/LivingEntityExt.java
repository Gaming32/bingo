package io.github.gaming32.bingo.ext;

import io.github.gaming32.bingo.util.DamageEntry;
import net.minecraft.world.damagesource.DamageSource;

import java.util.function.Predicate;

public interface LivingEntityExt {
    void bingo$recordDamage(DamageSource source);
    boolean bingo$hasOnlyBeenDamagedBy(Predicate<DamageEntry> damageTypePredicate);

}
