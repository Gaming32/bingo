package io.github.gaming32.bingo.platform.registrar;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public interface KeyMappingBuilder {
    KeyMappingBuilder name(String name);

    KeyMappingBuilder category(KeyMapping.Category category);

    KeyMappingBuilder keyType(InputConstants.Type type);

    KeyMappingBuilder keyCode(int keyCode);

    KeyMappingBuilder conflictContext(ConflictContext conflictContext);

    KeyMappingExt register(Consumer<Minecraft> action);

    KeyMapping.Category registerCategory(ResourceLocation id);

    enum ConflictContext {
        UNIVERSAL, GUI, IN_GAME, NEVER
    }

    record KeyMappingExt(KeyMapping mapping, ConflictContext conflictContext, Consumer<Minecraft> action) {
    }
}
