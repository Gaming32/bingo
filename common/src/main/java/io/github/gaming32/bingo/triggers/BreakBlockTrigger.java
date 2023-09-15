package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class BreakBlockTrigger extends SimpleCriterionTrigger<BreakBlockTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation(Bingo.MOD_ID, "break_block");

    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, LocationPredicate.fromJson(json.get("location")), ItemPredicate.fromJson(json.get("tool")));
    }

    @Override
    @NotNull
    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player, BlockPos pos, ItemStack tool) {
        trigger(player, triggerInstance -> triggerInstance.matches(player.serverLevel(), pos, tool));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;
        private final ItemPredicate tool;

        public TriggerInstance(ContextAwarePredicate player, LocationPredicate location, ItemPredicate tool) {
            super(ID, player);
            this.location = location;
            this.tool = tool;
        }

        @Override
        @NotNull
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("location", location.serializeToJson());
            json.add("tool", tool.serializeToJson());
            return json;
        }

        public boolean matches(ServerLevel level, BlockPos pos, ItemStack tool) {
            return location.matches(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) && this.tool.matches(tool);
        }
    }

    public static final class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private LocationPredicate location = LocationPredicate.ANY;
        private ItemPredicate tool = ItemPredicate.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
            return this;
        }

        public Builder location(LocationPredicate location) {
            this.location = location;
            return this;
        }

        public Builder block(BlockPredicate block) {
            return location(LocationPredicate.Builder.location().setBlock(block).build());
        }

        public Builder block(Block block) {
            return block(BlockPredicate.Builder.block().of(block).build());
        }

        public Builder block(TagKey<Block> blockTag) {
            return block(BlockPredicate.Builder.block().of(blockTag).build());
        }

        public Builder tool(ItemPredicate tool) {
            this.tool = tool;
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, location, tool);
        }
    }
}
