package io.github.gaming32.bingo.platform.registrar;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

import java.util.function.Function;

public interface PictureInPictureRendererRegistrar {
    <S extends PictureInPictureRenderState> void register(Class<S> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> factory);
}
