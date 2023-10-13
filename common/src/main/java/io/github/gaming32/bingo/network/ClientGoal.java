package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.game.ActiveGoal;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record ClientGoal(
    ResourceLocation id, // Used for advanced tooltips
    Component name,
    @Nullable Component tooltip,
    @Nullable ResourceLocation tooltipIcon,
    GoalIcon icon,
    BingoTag.SpecialType specialType,
    boolean hasProgress
) {
    public ClientGoal(ActiveGoal goal) {
        this(
            goal.getGoal().getId(),
            goal.getName(),
            goal.getTooltip(),
            goal.getGoal().getTooltipIcon(),
            goal.getIcon(),
            goal.getGoal().getSpecialType(),
            goal.hasProgress()
        );
    }

    @SuppressWarnings("deprecation")
    public ClientGoal(FriendlyByteBuf buf) {
        this(
            buf.readResourceLocation(),
            buf.readComponent(),
            buf.readNullable(FriendlyByteBuf::readComponent),
            buf.readNullable(FriendlyByteBuf::readResourceLocation),
            buf.readWithCodecTrusted(NbtOps.INSTANCE, GoalIcon.CODEC),
            buf.readEnum(BingoTag.SpecialType.class),
            buf.readBoolean()
        );
    }

    @SuppressWarnings("deprecation")
    public void serialize(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeComponent(name);
        buf.writeNullable(tooltip, FriendlyByteBuf::writeComponent);
        buf.writeNullable(tooltipIcon, FriendlyByteBuf::writeResourceLocation);
        buf.writeWithCodec(NbtOps.INSTANCE, GoalIcon.CODEC, icon);
        buf.writeEnum(specialType);
        buf.writeBoolean(hasProgress);
    }
}
