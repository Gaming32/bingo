package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public record EffectIcon(MobEffect effect, Potion potion) implements GoalIcon {
    public static final MapCodec<EffectIcon> CODEC = BuiltInRegistries.MOB_EFFECT
        .byNameCodec()
        .xmap(EffectIcon::of, EffectIcon::effect)
        .fieldOf("effect");

    public static EffectIcon of(MobEffect effect) {
        Potion potion = Potions.EMPTY;
        outer: for (Potion p : BuiltInRegistries.POTION) {
            for (MobEffectInstance e : p.getEffects()) {
                if (e.getEffect() == effect) {
                    potion = p;
                    break outer;
                }
            }
        }

        return new EffectIcon(effect, potion);
    }

    @Override
    public ItemStack item() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.EFFECT.get();
    }
}
