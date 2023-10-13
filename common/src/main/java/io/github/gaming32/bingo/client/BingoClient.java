package io.github.gaming32.bingo.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientScreenInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.icons.DefaultIconRenderers;
import io.github.gaming32.bingo.client.icons.IconRenderer;
import io.github.gaming32.bingo.client.icons.IconRenderers;
import io.github.gaming32.bingo.client.recipeviewer.RecipeViewerPlugin;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.BingoNetwork;
import io.github.gaming32.bingo.network.ClientGoal;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class BingoClient {
    private static final ResourceLocation BOARD_TEXTURE = new ResourceLocation("bingo:board");
    public static final Component BOARD_TITLE = Component.translatable("bingo.board.title");

    public static final int BOARD_OFFSET = 3;

    public static BingoBoard.Teams clientTeam = BingoBoard.Teams.NONE;
    public static ClientBoard clientBoard;

    private static BingoClientConfig config;
    private static RecipeViewerPlugin recipeViewerPlugin;

    public static void init() {
        AutoConfig.register(BingoClientConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(BingoClientConfig.class).getConfig();

        if (!Platform.isFabric()) {
            Platform.getMod(Bingo.MOD_ID).registerConfigurationScreen(
                parent -> AutoConfig.getConfigScreen(BingoClientConfig.class, parent).get()
            );
        }

        ClientGuiEvent.RENDER_HUD.register((graphics, tickDelta) -> {
            if (clientBoard == null) return;
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.getDebugOverlay().showDebugScreen() || minecraft.screen instanceof BoardScreen) return;
            final float scale = config.board.scale;
            final float x = config.board.corner.getX(graphics.guiWidth(), scale);
            final float y = config.board.corner.getY(graphics.guiHeight(), scale);
            renderBingo(graphics, minecraft.screen instanceof ChatScreen, x, y, scale);
        });

        ClientScreenInputEvent.KEY_RELEASED_PRE.register((client, screen, keyCode, scanCode, modifiers) -> {
            if (clientBoard == null || !(screen instanceof ChatScreen)) {
                return EventResult.pass();
            }
            final float scale = config.board.scale;
            final float x = config.board.corner.getX(client.getWindow().getGuiScaledWidth(), scale);
            final float y = config.board.corner.getY(client.getWindow().getGuiScaledHeight(), scale);
            return detectPress(keyCode, scanCode, x, y, scale) ? EventResult.interruptTrue() : EventResult.pass();
        });

        ClientScreenInputEvent.MOUSE_RELEASED_PRE.register((client, screen, mouseX, mouseY, button) -> {
            if (clientBoard == null || !(screen instanceof ChatScreen)) {
                return EventResult.pass();
            }
            final float scale = config.board.scale;
            final float x = config.board.corner.getX(client.getWindow().getGuiScaledWidth(), scale);
            final float y = config.board.corner.getY(client.getWindow().getGuiScaledHeight(), scale);
            return detectClick(button, x, y, scale) ? EventResult.interruptTrue() : EventResult.pass();
        });

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            clientTeam = BingoBoard.Teams.NONE;
            clientBoard = null;
        });

        // Mark as able to receive this in PLAY, for mod presence checks
        NetworkManager.registerReceiver(NetworkManager.s2c(), BingoNetwork.PROTOCOL_VERSION_PACKET, (buf, context) -> {});

        DefaultIconRenderers.setup();

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

    public static BingoClientConfig getConfig() {
        return config;
    }

    public static int getBoardWidth() {
        return 14 + 18 * clientBoard.size();
    }

    public static int getBoardHeight() {
        return 24 + 18 * clientBoard.size();
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

        final BingoMousePos mousePos = mouseHover ? BingoMousePos.getPos(minecraft, clientBoard.size(), x, y, scale) : null;

        graphics.blitSprite(
            BOARD_TEXTURE, 0, 0,
            7 + 18 * clientBoard.size() + 7,
            17 + 18 * clientBoard.size() + 7
        );
        graphics.drawString(minecraft.font, BOARD_TITLE, 8, 6, 0x404040, false);

        for (int sx = 0; sx < clientBoard.size(); sx++) {
            for (int sy = 0; sy < clientBoard.size(); sy++) {
                final ClientGoal goal = clientBoard.getGoal(sx, sy);
                final int slotX = sx * 18 + 8;
                final int slotY = sy * 18 + 18;
                final GoalIcon icon = goal.icon();
                final IconRenderer<? super GoalIcon> renderer = IconRenderers.getRenderer(icon);
                renderer.render(icon, graphics, slotX, slotY);
                renderer.renderDecorations(icon, minecraft.font, graphics, slotX, slotY);
                final BingoBoard.Teams state = clientBoard.getState(sx, sy);
                boolean isGoalCompleted = state.and(clientTeam);
                final int color = isGoalCompleted ? 0x55ff55 : goal.specialType().incompleteColor;
                if (color != 0) {
                    graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xA0000000 | color);
                }

                GoalProgress progress = clientBoard.getProgress(sx, sy);
                if (progress != null && !isGoalCompleted && progress.progress() > 0) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(0, 0, 200);

                    final int pWidth = Math.round(progress.progress() * 13f / progress.maxProgress());
                    final int pColor = Mth.hsvToRgb(
                        (float)progress.progress() / progress.maxProgress() / 3f, 1f, 1f
                    );
                    final int pX = slotX + 2;
                    final int pY = slotY + 13;
                    graphics.fill(RenderType.guiOverlay(), pX, pY, pX + 13, pY + 2, 0xff000000);
                    graphics.fill(RenderType.guiOverlay(), pX, pY, pX + pWidth, pY + 1, pColor | 0xff000000);

                    graphics.pose().popPose();
                }
            }
        }

        if (BingoMousePos.hasSlotPos(mousePos)) {
            graphics.pose().pushPose();
            graphics.pose().translate(0f, 0f, 200f);
            final int slotX = mousePos.slotIdX() * 18 + 8;
            final int slotY = mousePos.slotIdY() * 18 + 18;
            graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80ffffff);
            graphics.pose().popPose();
        }

        graphics.pose().popPose();
        if (BingoMousePos.hasSlotPos(mousePos)) {
            final ClientGoal goal = clientBoard.getGoal(mousePos.slotIdX(), mousePos.slotIdY());
            final GoalProgress progress = clientBoard.getProgress(mousePos.slotIdX(), mousePos.slotIdY());
            final List<Component> lines = new ArrayList<>(3);
            lines.add(goal.name());
            if (progress != null && (progress.maxProgress() > 1 || minecraft.options.advancedItemTooltips)) {
                lines.add(Component.translatable("bingo.progress", progress.progress(), progress.maxProgress()));
            }
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
                (int)mousePos.mouseX(), (int)mousePos.mouseY()
            );
            graphics.flush();
        }
    }

    public static boolean detectClick(int button, float x, float y, float scale) {
        return detectClickOrPress(InputConstants.Type.MOUSE.getOrCreate(button), x, y, scale);
    }

    public static boolean detectPress(int keyCode, int scanCode, float x, float y, float scale) {
        return detectClickOrPress(InputConstants.getKey(keyCode, scanCode), x, y, scale);
    }

    public static boolean detectClickOrPress(InputConstants.Key key, float x, float y, float scale) {
        if (clientBoard == null) {
            return false;
        }

        final BingoMousePos mousePos = BingoMousePos.getPos(Minecraft.getInstance(), clientBoard.size(), x, y, scale);
        if (!mousePos.hasSlotPos()) {
            return false;
        }

        final ClientGoal goal = clientBoard.getGoal(mousePos.slotIdX(), mousePos.slotIdY());

        final RecipeViewerPlugin plugin = getRecipeViewerPlugin();
        if (plugin.isViewRecipe(key)) {
            plugin.showRecipe(goal.icon().item());
            return true;
        }
        if (plugin.isViewUsages(key)) {
            plugin.showUsages(goal.icon().item());
            return true;
        }
        return false;
    }
}
