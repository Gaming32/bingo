package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryBuilder;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface GoalIconType<I extends GoalIcon> {
    DeferredRegister<GoalIconType<?>> REGISTER = new RegistryBuilder("goal_icon_type")
        .synced()
        .defaultId("empty")
        .build();

    RegistryValue<GoalIconType<EmptyIcon>> EMPTY = register("empty", EmptyIcon.CODEC, EmptyIcon.STREAM_CODEC);
    RegistryValue<GoalIconType<BlockIcon>> BLOCK = register("block", BlockIcon.CODEC, BlockIcon.STREAM_CODEC);
    RegistryValue<GoalIconType<CycleIcon>> CYCLE = register("cycle", CycleIcon.CODEC, CycleIcon.STREAM_CODEC);
    RegistryValue<GoalIconType<EffectIcon>> EFFECT = register("effect", EffectIcon.CODEC, EffectIcon.STREAM_CODEC);
    RegistryValue<GoalIconType<EntityIcon>> ENTITY = register("entity", EntityIcon.CODEC, EntityIcon.STREAM_CODEC);
    RegistryValue<GoalIconType<EntityTypeTagCycleIcon>> ENTITY_TYPE_TAG_CYCLE = register("entity_type_tag_cycle", EntityTypeTagCycleIcon.CODEC, EntityTypeTagCycleIcon.STREAM_CODEC);
    RegistryValue<GoalIconType<IndicatorIcon>> INDICATOR = register("indicator", IndicatorIcon.CODEC, IndicatorIcon.STREAM_CODEC);
    RegistryValue<GoalIconType<InstrumentCycleIcon>> INSTRUMENT_CYCLE = register("instrument_cycle", InstrumentCycleIcon.CODEC, InstrumentCycleIcon.STREAM_CODEC);
    RegistryValue<GoalIconType<ItemIcon>> ITEM = register("item", ItemIcon.CODEC, ItemIcon.STREAM_CODEC);
    RegistryValue<GoalIconType<ItemTagCycleIcon>> ITEM_TAG_CYCLE = register("item_tag_cycle", ItemTagCycleIcon.CODEC, ItemTagCycleIcon.STREAM_CODEC);

    MapCodec<I> codec();

    StreamCodec<? super RegistryFriendlyByteBuf, I> streamCodec();

    static <I extends GoalIcon> RegistryValue<GoalIconType<I>> register(
        String id,
        MapCodec<I> codec,
        StreamCodec<? super RegistryFriendlyByteBuf, I> streamCodec
    ) {
        return REGISTER.register(id, () -> new GoalIconType<>() {
            @Override
            public MapCodec<I> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, I> streamCodec() {
                return streamCodec;
            }

            @Override
            public String toString() {
                return "GoalIconType[" + id + "]";
            }
        });
    }

    static void load() {
    }
}
