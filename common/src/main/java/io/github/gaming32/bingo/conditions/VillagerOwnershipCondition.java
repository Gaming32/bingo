package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.CustomEnumCodec;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record VillagerOwnershipCondition(PoiManager.Occupancy occupancy, Optional<TagPredicate<PoiType>> tag) implements LootItemCondition {
    public static final CustomEnumCodec<PoiManager.Occupancy> OCCUPANCY_CODEC = CustomEnumCodec.of(PoiManager.Occupancy.class);
    public static final Codec<TagPredicate<PoiType>> TAG_CODEC = TagPredicate.codec(Registries.POINT_OF_INTEREST_TYPE);
    public static final Codec<VillagerOwnershipCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            OCCUPANCY_CODEC.codec().fieldOf("occupancy").forGetter(VillagerOwnershipCondition::occupancy),
            ExtraCodecs.strictOptionalField(TAG_CODEC, "tag").forGetter(VillagerOwnershipCondition::tag)
        ).apply(instance, VillagerOwnershipCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.VILLAGER_OWNERSHIP.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.getLevel().getPoiManager().getInChunk(
            type -> tag.isEmpty() || tag.get().matches(type),
            new ChunkPos(BlockPos.containing(lootContext.getParam(LootContextParams.ORIGIN))),
            occupancy
        ).anyMatch(r -> true);
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }
}
