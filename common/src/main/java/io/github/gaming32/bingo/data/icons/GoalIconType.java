package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.options.DefaultIdRegistrarOption;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface GoalIconType<I extends GoalIcon> {
    ResourceKey<Registry<GoalIconType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(
        new ResourceLocation("bingo:goal_icon_type")
    );
    Registrar<GoalIconType<?>> REGISTRAR = Bingo.REGISTRAR_MANAGER
        .<GoalIconType<?>>builder(REGISTRY_KEY.location())
        .option(new DefaultIdRegistrarOption(new ResourceLocation("bingo:empty")))
        .syncToClients()
        .build();

    RegistrySupplier<GoalIconType<EmptyIcon>> EMPTY = register("empty", EmptyIcon.CODEC);
    RegistrySupplier<GoalIconType<BlockIcon>> BLOCK = register("block", BlockIcon.CODEC);
    RegistrySupplier<GoalIconType<CycleIcon>> CYCLE = register("cycle", CycleIcon.CODEC);
    RegistrySupplier<GoalIconType<EffectIcon>> EFFECT = register("effect", EffectIcon.CODEC);
    RegistrySupplier<GoalIconType<EntityIcon>> ENTITY = register("entity", EntityIcon.CODEC);
    RegistrySupplier<GoalIconType<EntityTypeTagCycleIcon>> ENTITY_TYPE_TAG_CYCLE = register("entity_type_tag_cycle", EntityTypeTagCycleIcon.CODEC);
    RegistrySupplier<GoalIconType<IndicatorIcon>> INDICATOR = register("indicator", IndicatorIcon.CODEC);
    RegistrySupplier<GoalIconType<ItemIcon>> ITEM = register("item", ItemIcon.CODEC);
    RegistrySupplier<GoalIconType<ItemTagCycleIcon>> ITEM_TAG_CYCLE = register("item_tag_cycle", ItemTagCycleIcon.CODEC);

    MapCodec<I> codec();

    static <I extends GoalIcon> RegistrySupplier<GoalIconType<I>> register(String id, MapCodec<I> codec) {
        if (id.indexOf(':') < 0) {
            id = "bingo:" + id;
        }
        final ResourceLocation location = new ResourceLocation(id);
        return REGISTRAR.register(location, () -> new GoalIconType<>() {
            @Override
            public MapCodec<I> codec() {
                return codec;
            }

            @Override
            public String toString() {
                return "GoalIconType[" + location + "]";
            }
        });
    }

    static void load() {
    }
}
