package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.game.ActiveGoal;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ClientGoal(Component name, @Nullable Component tooltip, ItemStack icon) {
    public ClientGoal(ActiveGoal goal) {
        this(goal.getName(), goal.getTooltip(), goal.getIcon());
    }

    public ClientGoal(FriendlyByteBuf buf) {
        this(buf.readComponent(), buf.readNullable(FriendlyByteBuf::readComponent), buf.readItem());
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeComponent(name);
        buf.writeNullable(tooltip, FriendlyByteBuf::writeComponent);
        buf.writeItem(icon);
    }

    public ItemStack toSingleStack() {
        final ItemStack result = icon.copy();
        result.setHoverName(name);
        if (tooltip != null) {
            final ListTag lore = new ListTag();
            lore.add(StringTag.valueOf(Component.Serializer.toJson(tooltip)));
            result.getOrCreateTagElement("display").put("Lore", lore);
        }
        return result;
    }
}
