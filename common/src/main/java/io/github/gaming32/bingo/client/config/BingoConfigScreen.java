package io.github.gaming32.bingo.client.config;

import io.github.gaming32.bingo.client.BingoClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

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
        graphics.drawCenteredString(font, title, width / 2, 15, 0xffffff);
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
}
