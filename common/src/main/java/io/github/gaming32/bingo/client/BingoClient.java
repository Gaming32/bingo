package io.github.gaming32.bingo.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.recipeviewer.RecipeViewerPlugin;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.ClientGoal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class BingoClient {
    private static final ResourceLocation BOARD_TEXTURE = new ResourceLocation("bingo:textures/gui/board.png");
    public static final Component BOARD_TITLE = Component.translatable("bingo.board.title");

    public static final int BOARD_WIDTH = 104;
    public static final int BOARD_HEIGHT = 114;
    public static final int BOARD_OFFSET = 3;

    public static BoardCorner boardCorner = BoardCorner.UPPER_RIGHT;
    public static float boardScale = 1f;

    public static BingoBoard.Teams clientTeam = BingoBoard.Teams.NONE;
    public static ClientBoard clientBoard;

    private static RecipeViewerPlugin recipeViewerPlugin;

    public static void init() {
        ClientGuiEvent.RENDER_HUD.register((graphics, tickDelta) -> {
            if (clientBoard == null) return;
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.options.renderDebug || minecraft.screen instanceof BoardScreen) return;
            final float scale = boardScale;
            final float x = boardCorner.getX(graphics, scale);
            final float y = boardCorner.getY(graphics, scale);
            renderBingo(graphics, minecraft.screen instanceof ChatScreen, x, y, scale);
        });

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            clientTeam = BingoBoard.Teams.NONE;
            clientBoard = null;
        });

        // TODO: Maybe figure out warning on Forge? Priority: Low
        final KeyMapping boardKey = new KeyMapping("bingo.key.board", InputConstants.KEY_B, "bingo.key.category");
        KeyMappingRegistry.register(boardKey);
        ClientTickEvent.CLIENT_PRE.register(instance -> {
            while (boardKey.consumeClick()) {
                if (clientBoard != null) {
                    instance.setScreen(new BoardScreen());
                }
            }
        });
    }

    public static RecipeViewerPlugin getRecipeViewerPlugin() {
        if (recipeViewerPlugin == null) {
            recipeViewerPlugin = RecipeViewerPlugin.detect();
        }
        return recipeViewerPlugin;
    }

    public static void renderBingo(GuiGraphics graphics, boolean mouseHover, float x, float y, float scale) {
        if (clientBoard == null) {
            Bingo.LOGGER.warn("BingoClient.renderBingo() called when Bingo.clientBoard == null!");
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1);
        graphics.pose().translate(x, y, 0);

        double mouseX = 0;
        double mouseY = 0;
        int slotIdX = -1;
        int slotIdY = -1;
        if (mouseHover) {
            mouseX = minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
            mouseY = minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
            final double relX = (mouseX - x / scale) * scale;
            final double relY = (mouseY - y / scale) * scale;
            final double slotIdXD = (relX - 7) / 18;
            final double slotIdYD = (relY - 17) / 18;

            if (slotIdXD >= 0 && slotIdXD < 5 && slotIdYD >= 0 && slotIdYD < 5) {
                slotIdX = (int)((relX - 7) / 18);
                slotIdY = (int)((relY - 17) / 18);
            }
        }

        graphics.blit(BOARD_TEXTURE, 0, 0, 0, 0, 128, 128, 128, 128);
        graphics.drawString(minecraft.font, BOARD_TITLE, 8, 6, 0x404040, false);

        for (int sx = 0; sx < BingoBoard.SIZE; sx++) {
            for (int sy = 0; sy < BingoBoard.SIZE; sy++) {
                final ClientGoal goal = clientBoard.getGoal(sx, sy);
                final int slotX = sx * 18 + 8;
                final int slotY = sy * 18 + 18;
                graphics.renderFakeItem(goal.icon(), slotX, slotY);
                graphics.renderItemDecorations(minecraft.font, goal.icon(), slotX, slotY);
                if (clientTeam.any()) {
                    final BingoBoard.Teams state = clientBoard.getState(sx, sy);
                    final int color = state.and(clientTeam) ? 0x55ff55 : goal.isNever() ? 0xff5555 : 0;
                    if (color != 0) {
                        graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xA0000000 | color);
                    }
                }
            }
        }

        if (slotIdX != -1) {
            graphics.pose().pushPose();
            graphics.pose().translate(0f, 0f, 200f);
            final int slotX = slotIdX * 18 + 8;
            final int slotY = slotIdY * 18 + 18;
            graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80ffffff);
            graphics.pose().popPose();
        }

        graphics.pose().popPose();
        if (slotIdX != -1) {
            final ClientGoal goal = clientBoard.getGoal(slotIdX, slotIdY);
            final List<Component> lines = new ArrayList<>(3);
            lines.add(goal.name());
            if (minecraft.options.advancedItemTooltips) {
                lines.add(Component.literal(goal.id().toString()).withStyle(ChatFormatting.DARK_GRAY));
            }
            if (goal.tooltip() != null) {
                lines.add(CommonComponents.EMPTY);
                lines.add(goal.tooltip());
            }
            graphics.renderTooltip(
                minecraft.font, lines,
                Optional.ofNullable(goal.tooltipIcon()).map(IconTooltip::new),
                (int)mouseX, (int)mouseY
            );

            final RecipeViewerPlugin plugin = getRecipeViewerPlugin();
            if (plugin.isViewRecipe(minecraft)) {
                plugin.showRecipe(goal.icon());
            } else if (plugin.isViewUsages(minecraft)) {
                plugin.showUsages(goal.icon());
            }
        }
    }
}
