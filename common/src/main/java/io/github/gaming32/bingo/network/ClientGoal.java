package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.game.ActiveGoal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ClientGoal(Component name, @Nullable Component tooltip, ItemStack icon, boolean isNever) {
    public ClientGoal(ActiveGoal goal) {
        this(goal.getName(), goal.getTooltip(), goal.getIcon(), goal.getGoal().getTagIds().contains(BingoTags.NEVER));
    }

    public ClientGoal(FriendlyByteBuf buf) {
        this(buf.readComponent(), buf.readNullable(FriendlyByteBuf::readComponent), readIcon(buf), buf.readBoolean());
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeComponent(name);
        buf.writeNullable(tooltip, FriendlyByteBuf::writeComponent);
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
