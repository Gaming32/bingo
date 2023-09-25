package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
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
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, GsonHelper.getAsInt(json, "count"));
    }

    public void trigger(ServerPlayer player, Level level, BlockPos pos) {
        trigger(player, triggerInstance -> triggerInstance.matches(level, pos));
    }

    public static TriggerInstance create(int count) {
        return new TriggerInstance(Optional.empty(), count);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final int count;

        public TriggerInstance(Optional<ContextAwarePredicate> player, int count) {
            super(player);
            this.count = count;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            result.addProperty("count", count);
            return result;
        }

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
