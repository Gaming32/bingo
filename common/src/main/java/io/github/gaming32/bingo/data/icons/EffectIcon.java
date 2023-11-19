package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public record EffectIcon(MobEffect effect, Potion potion) implements GoalIcon {
    public static final Codec<EffectIcon> CODEC = BingoCodecs.catchIAE(RecordCodecBuilder.create(instance ->
        instance.group(
            BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("effect").forGetter(EffectIcon::effect)
        ).apply(instance, EffectIcon::of)
    ));
    
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
