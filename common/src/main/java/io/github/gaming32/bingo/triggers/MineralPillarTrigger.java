package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MineralPillarTrigger extends SimpleCriterionTrigger<MineralPillarTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockGetter level, BlockPos pos) {
        trigger(player, instance -> instance.matches(level, pos));
    }

    public static Criterion<TriggerInstance> pillar(TagKey<Block> tag) {
        return BingoTriggers.MINERAL_PILLAR.createCriterion(
            new TriggerInstance(Optional.empty(), tag)
        );
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        TagKey<Block> tag
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(TriggerInstance::tag)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(BlockGetter level, BlockPos pos) {
            final int required = BuiltInRegistries.BLOCK.getTag(tag).map(HolderSet::size).orElse(0);

            BlockState state = getState(level, pos);
            if (state.is(tag)) {
                if (required <= 1) {
                    return true;
                }
            } else {
                return required == 0;
            }

            final Set<BlockState> found = new HashSet<>();
            found.add(state);

            final BlockPos.MutableBlockPos currentPos = pos.mutable();
            if (loop(level, currentPos, 1, required, found)) {
                return true;
            }
            currentPos.set(pos);
            //noinspection RedundantIfStatement
            if (loop(level, currentPos, -1, required, found)) {
                return true;
            }
            return false;
        }

        private boolean loop(
            BlockGetter level,
            BlockPos.MutableBlockPos currentPos,
            int direction,
            int required,
            Set<BlockState> found
        ) {
            while (true) {
                currentPos.setY(currentPos.getY() + direction);
                final BlockState state = getState(level, currentPos);
                if (!state.is(tag)) {
                    break;
                }
                if (!found.add(state)) {
                    break;
                }
                if (found.size() >= required) {
                    return true;
                }
            }
            return false;
        }

        private BlockState getState(BlockGetter level, BlockPos pos) {
            final BlockState result = level.getBlockState(pos);
            if (result.is(BingoBlockTags.COPPER_BLOCKS)) {
                return Blocks.COPPER_BLOCK.defaultBlockState();
            }
            return result;
        }
    }
}
