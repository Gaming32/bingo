package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.CustomEnumCodec;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record VillagerOwnershipCondition(PoiManager.Occupancy occupancy, Optional<TagPredicate<PoiType>> tag) implements LootItemCondition {
    public static final MapCodec<VillagerOwnershipCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            CustomEnumCodec.of(PoiManager.Occupancy.class).codec().fieldOf("occupancy").forGetter(VillagerOwnershipCondition::occupancy),
            TagPredicate.codec(Registries.POINT_OF_INTEREST_TYPE).optionalFieldOf("tag").forGetter(VillagerOwnershipCondition::tag)
        ).apply(instance, VillagerOwnershipCondition::new)
    );

    @Override
    public MapCodec<VillagerOwnershipCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.getLevel().getPoiManager().getInChunk(
            type -> tag.isEmpty() || tag.get().matches(type),
            ChunkPos.containing(BlockPos.containing(lootContext.getParameter(LootContextParams.ORIGIN))),
            occupancy
        ).anyMatch(r -> true);
    }

    @NotNull
    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }
}
