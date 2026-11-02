package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.platform.BingoPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public enum FlammableCondition implements LootItemCondition {
    INSTANCE;

    public static final MapCodec<FlammableCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<FlammableCondition> codec() {
        return CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext lootContext) {
        Vec3 origin = lootContext.getOptional(LootContextParams.ORIGIN);
        return origin != null && BingoPlatform.isFlammable(lootContext.getLevel(), BlockPos.containing(origin));
    }
}
