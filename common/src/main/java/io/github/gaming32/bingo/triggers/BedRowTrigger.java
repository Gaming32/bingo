package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

public class BedRowTrigger extends SimpleCriterionTrigger<BedRowTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Level level, BlockPos pos) {
        trigger(player, triggerInstance -> triggerInstance.matches(level, pos));
    }

    public static Criterion<TriggerInstance> create(int count) {
        return BingoTriggers.BED_ROW.createCriterion(
            new TriggerInstance(Optional.empty(), count)
        );
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        int count
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                Codec.INT.fieldOf("count").forGetter(TriggerInstance::count)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(Level level, BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BedBlock)) {
                return false;
            }

            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            // normalize towards the foot of the bed
            if (state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD) {
                Block headBlock = state.getBlock();
                pos = pos.relative(facing.getOpposite());
                state = level.getBlockState(pos);
                if (state.getBlock() != headBlock || state.getValue(BlockStateProperties.HORIZONTAL_FACING) != facing) {
                    return false;
                }
            }

            if (count <= 1) {
                return true;
            }

            EnumSet<DyeColor> foundColors = EnumSet.of(((BedBlock) state.getBlock()).getColor());

            // scan one direction until we encounter no bed or a color we've seen before
            BlockPos offsetPos = pos;
            while (true) {
                offsetPos = offsetPos.relative(facing.getClockWise());
                BlockState offsetState = level.getBlockState(offsetPos);
                if (!(offsetState.getBlock() instanceof BedBlock bedBlock)) {
                    break;
                }
                if (state.getValue(BlockStateProperties.BED_PART) != BedPart.FOOT || state.getValue(BlockStateProperties.HORIZONTAL_FACING) != facing) {
                    break;
                }
                if (!foundColors.add(bedBlock.getColor())) {
                    break;
                }
                if (foundColors.size() >= count) {
                    return true;
                }
            }

            // scan the other direction until we encounter no bed or a color we've seen before
            offsetPos = pos;
            while (true) {
                offsetPos = offsetPos.relative(facing.getCounterClockWise());
                BlockState offsetState = level.getBlockState(offsetPos);
                if (!(offsetState.getBlock() instanceof BedBlock bedBlock)) {
                    break;
                }
                if (state.getValue(BlockStateProperties.BED_PART) != BedPart.FOOT || state.getValue(BlockStateProperties.HORIZONTAL_FACING) != facing) {
                    break;
                }
                if (!foundColors.add(bedBlock.getColor())) {
                    break;
                }
                if (foundColors.size() >= count) {
                    return true;
                }
            }

            // we haven't encountered enough colors
            return false;
        }
    }
}
