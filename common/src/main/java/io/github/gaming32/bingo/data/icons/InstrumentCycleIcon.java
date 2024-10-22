package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalInt;

public record InstrumentCycleIcon(Holder<Item> instrumentItem, OptionalInt overrideCount) implements GoalIcon {
    public static final MapCodec<InstrumentCycleIcon> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Item.CODEC.fieldOf("instrument_item").forGetter(InstrumentCycleIcon::instrumentItem),
            BingoCodecs.optionalInt("override_count").forGetter(InstrumentCycleIcon::overrideCount)
        ).apply(instance, InstrumentCycleIcon::new)
    );

    public InstrumentCycleIcon(Holder<Item> instrumentItem) {
        this(instrumentItem, OptionalInt.empty());
    }

    @Override
    public ItemStack getFallback(RegistryAccess registries) {
        final var registry = registries.lookupOrThrow(Registries.INSTRUMENT);
        final var result = InstrumentItem.create(instrumentItem.value(), registry.getAny().orElse(null));
        result.setCount(overrideCount.orElse(registry.size()));
        return result;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.INSTRUMENT_CYCLE.get();
    }
}
