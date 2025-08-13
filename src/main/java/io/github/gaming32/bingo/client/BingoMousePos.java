package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.game.BoardShape;
import io.github.gaming32.bingo.util.Vec2i;
import net.minecraft.client.Minecraft;

public record BingoMousePos(double mouseX, double mouseY, int goalIndex) {
    public static BingoMousePos getPos(Minecraft minecraft, BoardShape shape, int size, PositionAndScale boardPos) {
        final double mouseX = minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
        final double mouseY = minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
        final double relX = (mouseX - boardPos.x() * boardPos.scale()) / boardPos.scale();
        final double relY = (mouseY - boardPos.y() * boardPos.scale()) / boardPos.scale();
        final double slotIdXD = (relX - 7) / 18;
        final double slotIdYD = (relY - 17) / 18;

        int goalIndex = -1;

        Vec2i visualSize = shape.getVisualSize(size);
        if (slotIdXD >= 0 && slotIdXD < visualSize.x() && slotIdYD >= 0 && slotIdYD < visualSize.y()) {
            int slotIdX = (int)((relX - 7) / 18);
            int slotIdY = (int)((relY - 17) / 18);
            goalIndex = shape.getCellFromCoords(size, slotIdX, slotIdY);
        }

        return new BingoMousePos(mouseX, mouseY, goalIndex);
    }

    public boolean hasSlotPos() {
        return goalIndex != -1;
    }

    public Vec2i getSlotPos(ClientGame game) {
        return game.shape().getCoords(game.size(), goalIndex);
    }

    public static boolean hasSlotPos(BingoMousePos pos) {
        return pos != null && pos.hasSlotPos();
    }
}
