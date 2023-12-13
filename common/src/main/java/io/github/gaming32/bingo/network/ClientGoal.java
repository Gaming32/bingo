package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.game.ActiveGoal;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ClientGoal(
    ResourceLocation id, // Used for advanced tooltips
    Component name,
    Optional<Component> tooltip,
    Optional<ResourceLocation> tooltipIcon,
    GoalIcon icon,
    BingoTag.SpecialType specialType
) {
    public ClientGoal(ActiveGoal goal) {
        this(
            goal.goal().id(),
            goal.name(),
            goal.tooltip(),
            goal.goal().goal().getTooltipIcon(),
            goal.icon(),
            goal.goal().goal().getSpecialType()
        );
    }

    @SuppressWarnings("deprecation")
    public ClientGoal(FriendlyByteBuf buf) {
        this(
            buf.readResourceLocation(),
            buf.readComponent(),
            buf.readOptional(FriendlyByteBuf::readComponent),
            buf.readOptional(FriendlyByteBuf::readResourceLocation),
            buf.readWithCodecTrusted(NbtOps.INSTANCE, GoalIcon.CODEC),
            buf.readEnum(BingoTag.SpecialType.class)
        );
    }

    @SuppressWarnings("deprecation")
    public void serialize(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeComponent(name);
        buf.writeOptional(tooltip, FriendlyByteBuf::writeComponent);
        buf.writeOptional(tooltipIcon, FriendlyByteBuf::writeResourceLocation);
        buf.writeWithCodec(NbtOps.INSTANCE, GoalIcon.CODEC, icon);
        buf.writeEnum(specialType);
    }
}
