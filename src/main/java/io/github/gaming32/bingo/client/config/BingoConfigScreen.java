package io.github.gaming32.bingo.client.config;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.ext.RowHelperExt;
import io.github.gaming32.bingo.game.BingoBoard;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class BingoConfigScreen extends Screen {
    private final Screen parent;

    public BingoConfigScreen(Screen parent) {
        super(Component.translatable("bingo.client_config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        final GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
        final GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);

        rowHelper.addChild(createCornerButton());
        rowHelper.addChild(createSizeSlider());
        rowHelper.addChild(createShowScoreCounterButton());

        ((RowHelperExt) (Object) rowHelper).bingo$ensureNewLine();

        for (int slot = 0; slot < BingoBoard.NUM_MANUAL_HIGHLIGHT_COLORS; slot++) {
            addManualHighlightColorTextField(rowHelper, slot);
        }

        rowHelper.addChild(
            Button.builder(CommonComponents.GUI_DONE, button -> minecraft.setScreen(parent)).width(200).build(),
            2, rowHelper.newCellSettings().paddingTop(2)
        );
        rowHelper.addChild(
            Button.builder(
                Component.translatable("controls.reset"),
                button -> {
                    BingoClient.CONFIG.reset();
                    init();
                    BingoClient.CONFIG.save();
                }
            ).width(200).build(),
            2, rowHelper.newCellSettings().paddingTop(2)
        );
        gridLayout.arrangeElements();
        FrameLayout.alignInRectangle(gridLayout, 0, height / 6 - 12, width, height, 0.5f, 0f);
        gridLayout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 15, 0xffffffff);
    }

    private CycleButton<BoardCorner> createCornerButton() {
        return CycleButton.builder(BoardCorner::getDescription)
            .withValues(BoardCorner.values())
            .withInitialValue(BingoClient.CONFIG.getBoardCorner())
            .create(
                0, 0, 150, 20, Component.translatable("bingo.client_config.board_corner"),
                (button, corner) -> {
                    BingoClient.CONFIG.setBoardCorner(corner);
                    BingoClient.CONFIG.save();
                }
            );
    }

    private AbstractSliderButton createSizeSlider() {
        return new AbstractSliderButton(
            0, 0, 150, 20, CommonComponents.EMPTY,
            (Math.log10(BingoClient.CONFIG.getBoardScale()) + 1) / 2
        ) {
            private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.##");

            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("bingo.client_config.board_scale", NUMBER_FORMAT.format(getRealValue())));
            }

            @Override
            protected void applyValue() {
                BingoClient.CONFIG.setBoardScale((float)getRealValue());
                BingoClient.CONFIG.save();
            }

            private double getRealValue() {
                return Math.pow(10, value * 2 - 1);
            }
        };
    }

    private CycleButton<Boolean> createShowScoreCounterButton() {
        return CycleButton.onOffBuilder()
            .withInitialValue(BingoClient.CONFIG.isShowScoreCounter())
            .withTooltip(v -> Tooltip.create(Component.translatable("bingo.client_config.show_score_counter.tooltip")))
            .create(
                0, 0, 150, 20, Component.translatable("bingo.client_config.show_score_counter"),
                (button, show) -> {
                    BingoClient.CONFIG.setShowScoreCounter(show);
                    BingoClient.CONFIG.save();
                }
            );
    }

    private void addManualHighlightColorTextField(GridLayout.RowHelper rowHelper, int highlightValue) {
        Component label = Bingo.translatable("bingo.client_config.manual_highlight_color", highlightValue + 1);
        int color = BingoClient.CONFIG.getManualHighlightColor(highlightValue) & 0xffffff;
        EditBox editBox = new EditBox(font, 150, 20, label);
        editBox.setValue("%06X".formatted(color));
        ColorBox colorBox = new ColorBox(font, 20, 20, label, color);
        editBox.setResponder(value -> {
            int newColor;
            try {
                newColor = Integer.parseInt(value, 16) & 0xffffff;
            } catch (NumberFormatException e) {
                editBox.setTextColor(CommonColors.RED);
                return;
            }
            editBox.setTextColor(0xffe0e0e0);
            colorBox.color = newColor;
            BingoClient.CONFIG.setManualHighlightColor(highlightValue, newColor);
            BingoClient.CONFIG.save();
        });
        rowHelper.addChild(colorBox);
        rowHelper.addChild(editBox);
    }

    private static class ColorBox extends AbstractWidget {
        private final Font font;
        private final Component label;
        private int color;

        public ColorBox(Font font, int width, int height, Component label, int color) {
            super(0, 0, font.width(label) + 5 + width, height, Component.empty());
            this.font = font;
            this.label = label;
            this.color = color;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int labelWidth = font.width(label);
            graphics.drawString(font, label, getX(), getY() + (getHeight() - font.lineHeight) / 2, CommonColors.WHITE);

            int boxLeft = getX() + labelWidth + 5;
            graphics.fill(boxLeft, getY(), getRight() - 1, getBottom(), ARGB.color(0xff, color));
            graphics.hLine(boxLeft, getRight() - 1, getY(), CommonColors.BLACK);
            graphics.hLine(boxLeft, getRight() - 1, getBottom() - 1, CommonColors.BLACK);
            graphics.vLine(boxLeft, getY(), getBottom() - 1, CommonColors.BLACK);
            graphics.vLine(getRight() - 1, getY(), getBottom() - 1, CommonColors.BLACK);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }
    }
}
