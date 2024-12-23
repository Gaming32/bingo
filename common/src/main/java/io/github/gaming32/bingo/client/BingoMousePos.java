package io.github.gaming32.bingo.client;

import net.minecraft.client.Minecraft;

public record BingoMousePos(double mouseX, double mouseY, int slotIdX, int slotIdY) {
    public static BingoMousePos getPos(Minecraft minecraft, int size, PositionAndScale boardPos) {
        final double mouseX = minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
        final double mouseY = minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
        final double relX = (mouseX - boardPos.x() * boardPos.scale()) / boardPos.scale();
        final double relY = (mouseY - boardPos.y() * boardPos.scale()) / boardPos.scale();
        final double slotIdXD = (relX - 7) / 18;
        final double slotIdYD = (relY - 17) / 18;

        int slotIdX = -1;
        int slotIdY = -1;
        if (slotIdXD >= 0 && slotIdXD < size && slotIdYD >= 0 && slotIdYD < size) {
            slotIdX = (int)((relX - 7) / 18);
            slotIdY = (int)((relY - 17) / 18);
        }

        return new BingoMousePos(mouseX, mouseY, slotIdX, slotIdY);
    }

    public boolean hasSlotPos() {
        return slotIdX != -1;
    }

    public static boolean hasSlotPos(BingoMousePos pos) {
        return pos != null && pos.hasSlotPos();
    }
}
