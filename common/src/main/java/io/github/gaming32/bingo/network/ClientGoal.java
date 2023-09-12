package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.game.ActiveGoal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ClientGoal(
    ResourceLocation id, // Used for advanced tooltips
    Component name,
    @Nullable Component tooltip,
    @Nullable ResourceLocation tooltipIcon,
    ItemStack icon,
    boolean isNever
) {
    public ClientGoal(ActiveGoal goal) {
        this(
            goal.getGoal().getId(),
            goal.getName(),
            goal.getTooltip(),
            goal.getGoal().getTooltipIcon(),
            goal.getIcon(),
            goal.getGoal().getTagIds().contains(BingoTags.NEVER)
        );
    }

    public ClientGoal(FriendlyByteBuf buf) {
        this(
            buf.readResourceLocation(),
            buf.readComponent(),
            buf.readNullable(FriendlyByteBuf::readComponent),
            buf.readNullable(FriendlyByteBuf::readResourceLocation),
            readIcon(buf),
            buf.readBoolean()
        );
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeComponent(name);
        buf.writeNullable(tooltip, FriendlyByteBuf::writeComponent);
        buf.writeNullable(tooltipIcon, FriendlyByteBuf::writeResourceLocation);
        writeIcon(buf, icon);
        buf.writeBoolean(isNever);
    }

    private static void writeIcon(FriendlyByteBuf buf, ItemStack icon) {
        buf.writeId(BuiltInRegistries.ITEM, icon.getItem());
        buf.writeVarInt(icon.getCount());
        buf.writeNbt(icon.getTag());
    }

    private static ItemStack readIcon(FriendlyByteBuf buf) {
        final ItemStack icon = new ItemStack(buf.readById(BuiltInRegistries.ITEM), buf.readVarInt());
        icon.setTag(buf.readNbt());
        return icon;
    }
}
