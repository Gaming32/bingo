package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryBuilder;
import io.github.gaming32.bingo.platform.registry.RegistryValue;

public interface GoalIconType<I extends GoalIcon> {
    DeferredRegister<GoalIconType<?>> REGISTER = new RegistryBuilder("goal_icon_type")
        .synced()
        .defaultId("empty")
        .build();

    RegistryValue<GoalIconType<EmptyIcon>> EMPTY = register("empty", EmptyIcon.CODEC);
    RegistryValue<GoalIconType<BlockIcon>> BLOCK = register("block", BlockIcon.CODEC);
    RegistryValue<GoalIconType<CycleIcon>> CYCLE = register("cycle", CycleIcon.CODEC);
    RegistryValue<GoalIconType<EffectIcon>> EFFECT = register("effect", EffectIcon.CODEC);
    RegistryValue<GoalIconType<EntityIcon>> ENTITY = register("entity", EntityIcon.CODEC);
    RegistryValue<GoalIconType<EntityTypeTagCycleIcon>> ENTITY_TYPE_TAG_CYCLE = register("entity_type_tag_cycle", EntityTypeTagCycleIcon.CODEC);
    RegistryValue<GoalIconType<IndicatorIcon>> INDICATOR = register("indicator", IndicatorIcon.CODEC);
    RegistryValue<GoalIconType<ItemIcon>> ITEM = register("item", ItemIcon.CODEC);
    RegistryValue<GoalIconType<ItemTagCycleIcon>> ITEM_TAG_CYCLE = register("item_tag_cycle", ItemTagCycleIcon.CODEC);

    MapCodec<I> codec();

    static <I extends GoalIcon> RegistryValue<GoalIconType<I>> register(String id, MapCodec<I> codec) {
        return REGISTER.register(id, () -> new GoalIconType<>() {
            @Override
            public MapCodec<I> codec() {
                return codec;
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
