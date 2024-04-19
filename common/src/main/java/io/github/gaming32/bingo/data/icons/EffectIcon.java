package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public record EffectIcon(MobEffect effect, Holder<Potion> potion) implements GoalIcon {
    public static final MapCodec<EffectIcon> CODEC = BuiltInRegistries.MOB_EFFECT
        .byNameCodec()
        .fieldOf("effect")
        .xmap(EffectIcon::of, EffectIcon::effect);

    public static EffectIcon of(MobEffect effect) {
        return new EffectIcon(
            effect, BuiltInRegistries.POTION.holders()
                .filter(p -> p.value().getEffects().stream().anyMatch(e -> e.getEffect() == effect))
                .findFirst()
                .map(h -> (Holder<Potion>)h)
                .orElse(Potions.WATER)
        );
    }

    @Override
    public ItemStack item() {
        final ItemStack result = new ItemStack(Items.POTION);
        result.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return result;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.EFFECT.get();
    }
}
