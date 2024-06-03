package io.github.gaming32.bingo.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.config.BingoClientConfig;
import io.github.gaming32.bingo.client.icons.DefaultIconRenderers;
import io.github.gaming32.bingo.client.icons.IconRenderer;
import io.github.gaming32.bingo.client.icons.IconRenderers;
import io.github.gaming32.bingo.client.recipeviewer.RecipeViewerPlugin;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGameMode;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.ClientGoal;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.event.ClientEvents;
import io.github.gaming32.bingo.platform.registrar.KeyMappingBuilder;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class BingoClient {
    private static final ResourceLocation BOARD_TEXTURE = ResourceLocations.bingo("board");
    public static final Component BOARD_TITLE = Component.translatable("bingo.board.title");
    public static final Component BOARD_TITLE_SHORT = Component.translatable("bingo.board.title.short");

    public static final int BOARD_OFFSET = 3;

    public static BingoBoard.Teams clientTeam = BingoBoard.Teams.NONE;
    public static BingoBoard.Teams receivedClientTeam = BingoBoard.Teams.NONE;
    public static ClientGame clientGame;

    public static final BingoClientConfig CONFIG = new BingoClientConfig(
        BingoPlatform.platform.getConfigDir().resolve("bingo-client.toml")
    );
    private static RecipeViewerPlugin recipeViewerPlugin;

    public static void init() {
        CONFIG.load();
        CONFIG.save();

        registerEventHandlers();

        DefaultIconRenderers.setup();

        BingoPlatform.platform.registerKeyMappings(builder -> {
            builder
                .name("bingo.key.board")
                .category("bingo.key.category")
                .keyCode(InputConstants.KEY_B)
                .conflictContext(KeyMappingBuilder.ConflictContext.IN_GAME)
                .register(minecraft -> {
                    if (clientGame != null) {
                        minecraft.setScreen(new BoardScreen());
                    }
                });
        });

        BingoPlatform.platform.registerClientTooltips(registrar -> registrar.register(IconTooltip.class, ClientIconTooltip::new));

        ClientPayloadHandler.init(new ClientPayloadHandlerImpl());

        Bingo.LOGGER.info("Bongo");
    }

    private static void registerEventHandlers() {
        ClientEvents.KEY_RELEASED_PRE.register((screen, keyCode, scanCode, modifiers) -> {
            if (clientGame == null || !(screen instanceof ChatScreen)) {
                return false;
            }
            return detectPress(keyCode, scanCode, getBoardPosition());
        });

        ClientEvents.MOUSE_RELEASED_PRE.register((screen, mouseX, mouseY, button) -> {
            if (clientGame == null || !(screen instanceof ChatScreen)) {
                return false;
            }
            return detectClick(button, getBoardPosition());
        });

        ClientEvents.PLAYER_QUIT.register(player -> {
            clientTeam = receivedClientTeam = BingoBoard.Teams.NONE;
            clientGame = null;
        });

        ClientEvents.CLIENT_TICK_END.register(minecraft -> {
            if (minecraft.player == null || !minecraft.player.isSpectator()) {
                clientTeam = receivedClientTeam;
            } else if (minecraft.player.isSpectator() && !clientTeam.any()) {
                clientTeam = BingoBoard.Teams.TEAM1;
            }
        });
    }

    public static PositionAndScale getBoardPosition() {
        final Window window = Minecraft.getInstance().getWindow();
        final float scale = CONFIG.getBoardScale();
        final float x = CONFIG.getBoardCorner().getX(window.getGuiScaledWidth(), scale);
        final float y = CONFIG.getBoardCorner().getY(window.getGuiScaledHeight(), scale);
        return new PositionAndScale(x, y, scale);
    }

    public static RecipeViewerPlugin getRecipeViewerPlugin() {
        if (recipeViewerPlugin == null) {
            recipeViewerPlugin = RecipeViewerPlugin.detect();
        }
        return recipeViewerPlugin;
    }

    public static void renderBoardOnHud(Minecraft minecraft, GuiGraphics graphics) {
        if (clientGame == null || minecraft.getDebugOverlay().showDebugScreen() || minecraft.screen instanceof BoardScreen) {
            return;
        }
        final PositionAndScale pos = getBoardPosition();
        renderBingo(graphics, minecraft.screen instanceof ChatScreen, pos);

        if (CONFIG.isShowScoreCounter() && clientGame.renderMode() == BingoGameMode.RenderMode.ALL_TEAMS) {
            class TeamValue {
                final BingoBoard.Teams team;
                int score;

                TeamValue(BingoBoard.Teams team) {
                    this.team = team;
                }
            }

            final TeamValue[] teams = new TeamValue[clientGame.teams().length];
            for (int i = 0; i < teams.length; i++) {
                teams[i] = new TeamValue(BingoBoard.Teams.fromOne(i));
            }

            int totalScore = 0;
            for (final BingoBoard.Teams state : clientGame.states()) {
                if (state.count() == 1) {
                    totalScore++;
                    teams[state.getFirstIndex()].score++;
                }
            }

            Arrays.sort(teams, Comparator.comparing(v -> -v.score)); // Sort in reverse

            final Font font = minecraft.font;
            final int scoreX = (int)(pos.x() * pos.scale() + getBoardWidth() * pos.scale() / 2);
            int scoreY;
            if (CONFIG.getBoardCorner().isOnBottom) {
                scoreY = (int)((pos.y() - BOARD_OFFSET) * pos.scale() - font.lineHeight);
            } else {
                scoreY = (int)(pos.y() * pos.scale() + (getBoardHeight() + BOARD_OFFSET) * pos.scale());
            }
            final int shift = CONFIG.getBoardCorner().isOnBottom ? -12 : 12;
            for (final TeamValue teamValue : teams) {
                if (teamValue.score == 0) break;
                final PlayerTeam team = clientGame.teams()[teamValue.team.getFirstIndex()];
                final MutableComponent leftText = getDisplayName(team).copy();
                final MutableComponent rightText = Component.literal(" - " + teamValue.score);
                if (team.getColor() != ChatFormatting.RESET) {
                    leftText.withStyle(team.getColor());
                    rightText.withStyle(team.getColor());
                }
                graphics.drawString(font, leftText, scoreX - font.width(leftText), scoreY, 0xffffffff);
                graphics.drawString(font, rightText, scoreX, scoreY, 0xffffffff);
                scoreY += shift;
            }

            final MutableComponent leftText = Component.translatable("bingo.unclaimed");
            final MutableComponent rightText = Component.literal(" - " + (clientGame.states().length - totalScore));
            graphics.drawString(font, leftText, scoreX - font.width(leftText), scoreY, 0xffffffff);
            graphics.drawString(font, rightText, scoreX, scoreY, 0xffffffff);
        }
    }

    public static int getBoardWidth() {
        return 14 + 18 * clientGame.size();
    }

    public static int getBoardHeight() {
        return 24 + 18 * clientGame.size();
    }

    public static void renderBingo(GuiGraphics graphics, boolean mouseHover, PositionAndScale pos) {
        if (clientGame == null) {
            Bingo.LOGGER.warn("BingoClient.renderBingo() called when Bingo.clientGame == null!");
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();

        graphics.pose().pushPose();
        graphics.pose().scale(pos.scale(), pos.scale(), 1);
        graphics.pose().translate(pos.x(), pos.y(), 0);

        final BingoMousePos mousePos = mouseHover ? BingoMousePos.getPos(minecraft, clientGame.size(), pos) : null;

        graphics.blitSprite(
            BOARD_TEXTURE, 0, 0,
            7 + 18 * clientGame.size() + 7,
            17 + 18 * clientGame.size() + 7
        );
        renderBoardTitle(graphics, minecraft.font);

        final boolean spectator = minecraft.player != null && minecraft.player.isSpectator();
        for (int sx = 0; sx < clientGame.size(); sx++) {
            for (int sy = 0; sy < clientGame.size(); sy++) {
                final ClientGoal goal = clientGame.getGoal(sx, sy);
                final int slotX = sx * 18 + 8;
                final int slotY = sy * 18 + 18;
                final GoalIcon icon = goal.icon();
                final IconRenderer<? super GoalIcon> renderer = IconRenderers.getRenderer(icon);
                renderer.render(icon, graphics, slotX, slotY);
                renderer.renderDecorations(icon, minecraft.font, graphics, slotX, slotY);
                final BingoBoard.Teams state = clientGame.getState(sx, sy);
                boolean isGoalCompleted = state.and(clientTeam);
                int incompleteColor = Objects.requireNonNullElse(goal.specialType().incompleteColor, 0);

                final int[] colors = switch (clientGame.renderMode()) {
                    case FANCY -> new int[] {(isGoalCompleted ? 0x55ff55 : incompleteColor)};
                    case ALL_TEAMS -> state.stream().map((team) -> clientGame.teams()[team].getColor().getColor()).toArray();
                };
                for (int i = 0; i < colors.length; ++i) {
                    int color = colors[i];
                    int start = 16 * i / colors.length;
                    int end = 16 * (i + 1) / colors.length;
                    int base = (colors.length == 1) ? 0xA0000000 : 0x50000000;
                    graphics.fill(slotX + start, slotY, slotX + end, slotY + 16, base | color);
                }

                GoalProgress progress = clientGame.getProgress(sx, sy);
                if (progress != null && !isGoalCompleted && progress.progress() > 0 && !spectator) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(0, 0, 200);

                    final int pWidth = Math.round(progress.progress() * 13f / progress.maxProgress());
                    final int pColor = Mth.hsvToRgb((float)progress.progress() / progress.maxProgress() / 3f, 1f, 1f);
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
            final ClientGoal goal = clientGame.getGoal(mousePos.slotIdX(), mousePos.slotIdY());
            final GoalProgress progress = clientGame.getProgress(mousePos.slotIdX(), mousePos.slotIdY());
            final TooltipBuilder tooltip = new TooltipBuilder();
            tooltip.add(goal.name());
            if (progress != null && (progress.maxProgress() > 1 || minecraft.options.advancedItemTooltips)) {
                tooltip.add(Component.translatable("bingo.progress", progress.progress(), progress.maxProgress()));
            }
            if (minecraft.options.advancedItemTooltips) {
                tooltip.add(Component.literal(goal.id().toString()).withStyle(ChatFormatting.DARK_GRAY));
            }
            goal.tooltip().ifPresent(component -> {
                final int width = Math.max(300, minecraft.font.width(goal.name()));
                tooltip.add(FormattedCharSequence.EMPTY);
                minecraft.font.split(component, width).forEach(tooltip::add);
            });
            goal.tooltipIcon().map(IconTooltip::new).ifPresent(tooltip::add);
            tooltip.draw(minecraft.font, graphics, (int)mousePos.mouseX(), (int)mousePos.mouseY());
        }
    }

    private static void renderBoardTitle(GuiGraphics graphics, Font font) {
        final int maxWidth = getBoardWidth() - 16;
        final FormattedCharSequence title = font.width(BOARD_TITLE) > maxWidth
            ? getVisualOrderWithEllipses(BOARD_TITLE_SHORT, font, maxWidth)
            : BOARD_TITLE.getVisualOrderText();
        graphics.drawString(font, title, 8, 6, 0x404040, false);
    }

    public static FormattedCharSequence getVisualOrderWithEllipses(Component text, Font font, int maxWidth) {
        final int textWidth = font.width(text);
        if (textWidth <= maxWidth) {
            return text.getVisualOrderText();
        }
        final FormattedText shortText = font.substrByWidth(text, maxWidth - font.width(CommonComponents.ELLIPSIS));
        final FormattedText combinedText = FormattedText.composite(shortText, CommonComponents.ELLIPSIS);
        return Language.getInstance().getVisualOrder(combinedText);
    }

    public static boolean detectClick(int button, PositionAndScale boardPos) {
        return detectClickOrPress(InputConstants.Type.MOUSE.getOrCreate(button), boardPos);
    }

    public static boolean detectPress(int keyCode, int scanCode, PositionAndScale boardPos) {
        return detectClickOrPress(InputConstants.getKey(keyCode, scanCode), boardPos);
    }

    public static boolean detectClickOrPress(InputConstants.Key key, PositionAndScale boardPos) {
        if (clientGame == null) {
            return false;
        }

        final BingoMousePos mousePos = BingoMousePos.getPos(Minecraft.getInstance(), clientGame.size(), boardPos);
        if (!mousePos.hasSlotPos()) {
            return false;
        }

        final ClientGoal goal = clientGame.getGoal(mousePos.slotIdX(), mousePos.slotIdY());

        final RecipeViewerPlugin plugin = getRecipeViewerPlugin();
        if (plugin.isViewRecipe(key)) {
            plugin.showRecipe(IconRenderers.getRenderer(goal.icon()).getIconItem(goal.icon()));
            return true;
        }
        if (plugin.isViewUsages(key)) {
            plugin.showUsages(IconRenderers.getRenderer(goal.icon()).getIconItem(goal.icon()));
            return true;
        }
        return false;
    }

    public static Component getDisplayName(PlayerTeam team) {
        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            final Iterator<PlayerInfo> players = team.getPlayers()
                .stream()
                .map(connection::getPlayerInfo)
                .filter(Objects::nonNull)
                .iterator();
            if (players.hasNext()) {
                final PlayerInfo playerInfo = players.next();
                if (!players.hasNext()) {
                    final ClientLevel level = Minecraft.getInstance().level;
                    if (level != null) {
                        final Player player = level.getPlayerByUUID(playerInfo.getProfile().getId());
                        if (player != null) {
                            return player.getName();
                        }
                    }
                    return Component.literal(playerInfo.getProfile().getName());
                }
            }
        }
        return team.getDisplayName();
    }
}
