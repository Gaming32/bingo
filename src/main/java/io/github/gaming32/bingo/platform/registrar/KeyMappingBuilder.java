package io.github.gaming32.bingo.platform.registrar;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public interface KeyMappingBuilder {
    KeyMappingBuilder name(String name);

    KeyMappingBuilder category(String category);

    KeyMappingBuilder keyType(InputConstants.Type type);

    KeyMappingBuilder keyCode(int keyCode);

    KeyMappingBuilder conflictContext(ConflictContext conflictContext);

    KeyMappingExt register(Consumer<Minecraft> action);

    enum ConflictContext {
        UNIVERSAL, GUI, IN_GAME
    }

    record KeyMappingExt(KeyMapping mapping, ConflictContext conflictContext, Consumer<Minecraft> action) {
    }
}
