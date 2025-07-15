package io.github.gaming32.bingo.platform.registrar;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.function.Function;

public interface PictureInPictureRendererRegistrar {
    <S extends PictureInPictureRenderState> void register(Class<S> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> factory);
}
