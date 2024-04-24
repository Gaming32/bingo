package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.game.ActiveGoal;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientGoal> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, ClientGoal::id,
        ComponentSerialization.STREAM_CODEC, ClientGoal::name,
        ComponentSerialization.OPTIONAL_STREAM_CODEC, ClientGoal::tooltip,
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), ClientGoal::tooltipIcon,
        GoalIcon.STREAM_CODEC, ClientGoal::icon,
        BingoTag.SpecialType.STREAM_CODEC, ClientGoal::specialType,
        ClientGoal::new
    );

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
}
