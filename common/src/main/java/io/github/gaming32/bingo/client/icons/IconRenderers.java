package io.github.gaming32.bingo.client.icons;

import dev.architectury.registry.registries.RegistrySupplier;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.icons.GoalIconType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public final class IconRenderers {
    private static final Map<ResourceLocation, IconRenderer<?>> RENDERERS = new HashMap<>();

    private IconRenderers() {
    }

    public static <I extends GoalIcon> void register(GoalIconType<I> iconType, IconRenderer<I> renderer) {
        final ResourceLocation id = GoalIconType.REGISTRAR.getId(iconType);
        if (id == null) {
            throw new IllegalArgumentException("Tried to register renderer for unregistered icon type " + iconType);
        }
        RENDERERS.put(GoalIconType.REGISTRAR.getId(iconType), renderer);
    }

    public static <I extends GoalIcon> void register(RegistrySupplier<GoalIconType<I>> iconType, IconRenderer<I> renderer) {
        RENDERERS.put(iconType.getId(), renderer);
    }

    public static <I extends GoalIcon> IconRenderer<I> getRenderer(GoalIconType<I> iconType) {
        final ResourceLocation id = GoalIconType.REGISTRAR.getId(iconType);
        if (id == null) {
            throw new NoSuchElementException("Unknown id for icon type " + iconType);
        }
        return getRenderer(id);
    }

    public static <I extends GoalIcon> IconRenderer<I> getRenderer(RegistrySupplier<GoalIconType<I>> iconType) {
        return getRenderer(iconType.getId());
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
