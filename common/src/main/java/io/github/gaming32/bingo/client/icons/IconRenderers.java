package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.icons.GoalIconType;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public final class IconRenderers {
    private static final Map<ResourceLocation, IconRenderer<?>> RENDERERS = new HashMap<>();

    private IconRenderers() {
    }

    public static <I extends GoalIcon> void register(GoalIconType<I> iconType, IconRenderer<I> renderer) {
        final ResourceLocation id = GoalIconType.REGISTER.registry().getKey(iconType);
        if (id == null) {
            throw new IllegalArgumentException("Tried to register renderer for unregistered icon type " + iconType);
        }
        RENDERERS.put(id, renderer);
    }

    public static <I extends GoalIcon> void register(RegistryValue<GoalIconType<I>> iconType, IconRenderer<I> renderer) {
        RENDERERS.put(iconType.id(), renderer);
    }

    public static <I extends GoalIcon> IconRenderer<I> getRenderer(GoalIconType<I> iconType) {
        final ResourceLocation id = GoalIconType.REGISTER.registry().getKey(iconType);
        if (id == null) {
            throw new NoSuchElementException("Unknown id for icon type " + iconType);
        }
        return getRenderer(id);
    }

    public static <I extends GoalIcon> IconRenderer<I> getRenderer(RegistryValue<GoalIconType<I>> iconType) {
        return getRenderer(iconType.id());
    }

    @SuppressWarnings("unchecked")
    public static <I extends GoalIcon> IconRenderer<I> getRenderer(I icon) {
        return (IconRenderer<I>)getRenderer(icon.type());
    }

    @SuppressWarnings("unchecked")
    private static <I extends GoalIcon> IconRenderer<I> getRenderer(ResourceLocation location) {
        final IconRenderer<?> renderer = RENDERERS.get(location);
        if (renderer == null) {
            throw new NoSuchElementException("Unknown renderer for icon type " + location);
        }
        return (IconRenderer<I>)renderer;
    }
}
