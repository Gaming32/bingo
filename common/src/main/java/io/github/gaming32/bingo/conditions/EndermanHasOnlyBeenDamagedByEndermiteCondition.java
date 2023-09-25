package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.ext.EnderManExt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

// TODO: Maybe make this condition more general?
public enum EndermanHasOnlyBeenDamagedByEndermiteCondition implements LootItemCondition {
    INSTANCE;

    public static final Codec<EndermanHasOnlyBeenDamagedByEndermiteCondition> CODEC = Codec.unit(INSTANCE);

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.ENDERMAN_HAS_ONLY_BEEN_DAMAGED_BY_ENDERMITE.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        return entity instanceof EnderManExt enderman && enderman.bingo$hasOnlyBeenDamagedByEndermite();
    }
}
