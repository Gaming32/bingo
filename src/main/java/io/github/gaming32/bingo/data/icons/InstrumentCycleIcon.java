package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.InstrumentComponent;

import java.util.OptionalInt;

public record InstrumentCycleIcon(Holder<Item> instrumentItem, OptionalInt overrideCount) implements GoalIcon {
    public static final MapCodec<InstrumentCycleIcon> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Item.CODEC.fieldOf("instrument_item").forGetter(InstrumentCycleIcon::instrumentItem),
            BingoCodecs.optionalPositiveInt("override_count").forGetter(InstrumentCycleIcon::overrideCount)
        ).apply(instance, InstrumentCycleIcon::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, InstrumentCycleIcon> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.ITEM), InstrumentCycleIcon::instrumentItem,
        ByteBufCodecs.OPTIONAL_VAR_INT, InstrumentCycleIcon::overrideCount,
        InstrumentCycleIcon::new
    );

    public InstrumentCycleIcon(Holder<Item> instrumentItem) {
        this(instrumentItem, OptionalInt.empty());
    }

    @Override
    public ItemStackTemplate getFallback(RegistryAccess registries) {
        final var registry = registries.lookupOrThrow(Registries.INSTRUMENT);
        final var result = registry.getAny()
            .map(instrument -> new ItemStackTemplate(
                instrumentItem,
                1,
                DataComponentPatch.builder().set(DataComponents.INSTRUMENT, new InstrumentComponent(instrument)).build()
            ))
            .orElseGet(() -> new ItemStackTemplate(instrumentItem, 1, DataComponentPatch.EMPTY));
        return result.withCount(Math.max(overrideCount.orElse(registry.size()), 1));
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.INSTRUMENT_CYCLE.get();
    }
}
