package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public record EffectIcon(Holder<MobEffect> effect, Holder<Potion> potion) implements GoalIcon.WithoutContext {
    public static final MapCodec<EffectIcon> CODEC = BuiltInRegistries.MOB_EFFECT
        .holderByNameCodec()
        .fieldOf("effect")
        .xmap(EffectIcon::of, EffectIcon::effect);
    public static final StreamCodec<RegistryFriendlyByteBuf, EffectIcon> STREAM_CODEC =
        ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT).map(EffectIcon::of, EffectIcon::effect);

    public static EffectIcon of(Holder<MobEffect> effect) {
        return new EffectIcon(
            effect, BuiltInRegistries.POTION.listElements()
                .filter(p -> p.value().getEffects().stream().anyMatch(e -> e.getEffect() == effect))
                .findFirst()
                .map(h -> (Holder<Potion>)h)
                .orElse(Potions.WATER)
        );
    }

    @Override
    public ItemStack getFallback() {
        return PotionContents.createItemStack(Items.POTION, potion);
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.EFFECT.get();
    }
}
