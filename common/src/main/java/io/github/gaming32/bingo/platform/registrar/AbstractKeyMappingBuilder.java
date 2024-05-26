package io.github.gaming32.bingo.platform.registrar;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractKeyMappingBuilder implements KeyMappingBuilder {
    protected final List<KeyMappingWithAction> mappings = new ArrayList<>();

    protected String name;
    protected String category;
    protected InputConstants.Type keyType;
    protected int keyCode;
    protected ConflictContext conflictContext;

    protected AbstractKeyMappingBuilder() {
        reset();
    }

    private void reset() {
        name = null;
        keyType = InputConstants.Type.KEYSYM;
        keyCode = -1;
        category = null;
        conflictContext = ConflictContext.UNIVERSAL;
    }

    @Override
    public KeyMappingBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public KeyMappingBuilder category(String category) {
        this.category = category;
        return this;
    }

    @Override
    public KeyMappingBuilder keyType(InputConstants.Type type) {
        this.keyType = type;
        return this;
    }

    @Override
    public KeyMappingBuilder keyCode(int keyCode) {
        this.keyCode = keyCode;
        return this;
    }

    @Override
    public KeyMappingBuilder conflictContext(ConflictContext conflictContext) {
        this.conflictContext = conflictContext;
        return this;
    }

    @Override
    public KeyMappingWithAction register(Consumer<Minecraft> action) {
        if (name == null) {
            throw new IllegalStateException("KeyMappingBuilder.name not set");
        }
        if (category == null) {
            throw new IllegalStateException("KeyMappingBuilder.category not set");
        }
        if (action == null) {
            throw new IllegalStateException("KeyMappingBuilder.action not set");
        }
        final var result = new KeyMappingWithAction(new KeyMapping(name, keyType, keyCode, category), action);
        mappings.add(result);
        return result;
    }

    public void registerAll(Consumer<KeyMapping> consumer) {
        mappings.stream().map(KeyMappingWithAction::mapping).forEach(consumer);
    }

    public void handleAll(Minecraft minecraft) {
        for (final KeyMappingWithAction mapping : mappings) {
            while (mapping.mapping().consumeClick()) {
                mapping.action().accept(minecraft);
            }
        }
    }
}
