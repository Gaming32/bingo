package io.github.gaming32.bingo.platform.registrar;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

import java.util.function.Supplier;

public interface PictureInPictureRendererRegistrar {
    <S extends PictureInPictureRenderState> void register(Class<S> stateClass, Supplier<PictureInPictureRenderer<S>> factory);
}
