package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface GoalIconType<I extends GoalIcon> {
    ResourceKey<Registry<GoalIconType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(
        new ResourceLocation("bingo:goal_icon_type")
    );
    Registrar<GoalIconType<?>> REGISTRAR = Bingo.REGISTRAR_MANAGER.<GoalIconType<?>>builder(REGISTRY_KEY.location())
        .syncToClients()
        .build();

    RegistrySupplier<GoalIconType<EmptyIcon>> EMPTY = register("empty", EmptyIcon.CODEC);
    RegistrySupplier<GoalIconType<ItemIcon>> ITEM = register("item", ItemIcon.CODEC);
    RegistrySupplier<GoalIconType<BlockIcon>> BLOCK = register("block", BlockIcon.CODEC);
    RegistrySupplier<GoalIconType<CycleIcon>> CYCLE = register("cycle", CycleIcon.CODEC);

    Codec<I> codec();

    static <I extends GoalIcon> RegistrySupplier<GoalIconType<I>> register(String id, Codec<I> codec) {
        if (id.indexOf(':') < 0) {
            id = "bingo:" + id;
        }
        final ResourceLocation location = new ResourceLocation(id);
        return REGISTRAR.register(location, () -> new GoalIconType<>() {
            @Override
            public Codec<I> codec() {
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
