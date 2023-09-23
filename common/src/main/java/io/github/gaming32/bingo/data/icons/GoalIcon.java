package io.github.gaming32.bingo.data.icons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public interface GoalIcon {
    Codec<GoalIcon> CODEC = BingoCodecs.registrarByName(GoalIconType.REGISTRAR)
        .dispatch(GoalIcon::type, GoalIconType::codec);

    /**
     * Used for rendering count, as well as for a fallback for Vanilla clients.
     */
    ItemStack item();

    @Environment(EnvType.CLIENT)
    void render(GuiGraphics graphics, int x, int y);

    GoalIconType<?> type();

    default JsonObject serializeToJson() {
        return Util.toJsonObject(CODEC, this);
    }

    static GoalIcon deserialize(JsonElement element) {
        return Util.fromJsonElement(CODEC, element);
    }
}
