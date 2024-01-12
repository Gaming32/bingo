package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record InStructureCondition(TagKey<Structure> structure) implements LootItemCondition {
    public static final Codec<InStructureCondition> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            TagKey.hashedCodec(Registries.STRUCTURE).fieldOf("structure").forGetter(InStructureCondition::structure)
        ).apply(instance, InStructureCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.IN_STRUCTURE.get();
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext lootContext) {
        final BlockPos pos = BlockPos.containing(lootContext.getParam(LootContextParams.ORIGIN));
        return lootContext.getLevel().structureManager().getStructureWithPieceAt(pos, structure).isValid();
    }
}
