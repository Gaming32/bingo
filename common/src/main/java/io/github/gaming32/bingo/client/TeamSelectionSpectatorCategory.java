package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

// Based off of TeleportToTeamMenuCategory
public class TeamSelectionSpectatorCategory implements SpectatorMenuCategory, SpectatorMenuItem {
    private static final ResourceLocation CATEGORY_SPRITE = ResourceLocations.bingo("spectator/team_selection");
    private static final Component CATEGORY_NAME = Component.translatable("bingo.spectator.team_selection");
    private static final Component CATEGORY_PROMPT = Component.translatable("bingo.spectator.team_selection.prompt");

    private final List<SpectatorMenuItem> items;

    public TeamSelectionSpectatorCategory() {
        items = createTeamEntries(BingoClient.clientGame);
    }

    private static List<SpectatorMenuItem> createTeamEntries(@Nullable ClientGame game) {
        if (game == null) {
            return List.of();
        }
        final MutableInt teamId = new MutableInt();
        return Arrays.stream(game.getTeams())
            .map(team -> TeamSelectionItem.create(game, team, BingoBoard.Teams.fromOne(teamId.getAndIncrement())))
            .filter(Objects::nonNull)
            .toList();
    }

    @NotNull
    @Override
    public List<SpectatorMenuItem> getItems() {
        return items;
    }

    @NotNull
    @Override
    public Component getPrompt() {
        return CATEGORY_PROMPT;
    }

    @Override
    public void selectItem(SpectatorMenu menu) {
        menu.selectCategory(this);
    }

    @NotNull
    @Override
    public Component getName() {
        return CATEGORY_NAME;
    }

    @Override
    public void renderIcon(GuiGraphics graphics, float shadeColor, int alpha) {
        graphics.blitSprite(CATEGORY_SPRITE, 0, 0, 16, 16);
    }

    @Override
    public boolean isEnabled() {
        return !items.isEmpty();
    }

    private record TeamSelectionItem(
        PlayerTeam playerTeam,
        BingoBoard.Teams teamState,
        @Nullable Supplier<PlayerSkin> iconSkin,
        Component displayName
    ) implements SpectatorMenuItem {
        @Nullable
        public static SpectatorMenuItem create(ClientGame game, PlayerTeam team, BingoBoard.Teams teamId) {
            final Minecraft minecraft = Minecraft.getInstance();

            final List<PlayerInfo> onlinePlayers = new ArrayList<>();
            for (final String playerName : team.getPlayers()) {
                final PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(playerName);
                if (playerInfo != null && playerInfo.getGameMode() != GameType.SPECTATOR) {
                    onlinePlayers.add(playerInfo);
                }
            }

            if (onlinePlayers.isEmpty()) {
                boolean hasAnyGoals = false;
                for (int i = 0; i < game.getStates().length; i++) {
                    boolean hasGoal = game.getStates()[i].and(teamId);
                    if (game.getGoals()[i].specialType() == BingoTag.SpecialType.NEVER) {
                        hasGoal = !hasGoal;
                    }
                    if (hasGoal) {
                        hasAnyGoals = true;
                        break;
                    }
                }
                if (!hasAnyGoals) {
                    return null;
                }
            }

            final Supplier<PlayerSkin> iconSkin;
            if (onlinePlayers.isEmpty()) {
                iconSkin = null;
            } else {
                final PlayerInfo teamFace = onlinePlayers.get(RandomSource.create().nextInt(onlinePlayers.size()));
                iconSkin = minecraft.getSkinManager().lookupInsecure(teamFace.getProfile());
            }

            return new TeamSelectionItem(team, teamId, iconSkin, BingoClient.getDisplayName(team));
        }

        @Override
        public void selectItem(SpectatorMenu menu) {
            BingoClient.clientTeam = teamState;
        }

        @NotNull
        @Override
        public Component getName() {
            return displayName;
        }

        @Override
        public void renderIcon(GuiGraphics graphics, float shadeColor, int alpha) {
            final Integer color = playerTeam.getColor().getColor();
            if (color != null) {
                final float red = (color >> 16 & 0xff) / 255f;
                final float green = (color >> 8 & 0xff) / 255f;
                final float blue = (color & 0xff) / 255f;
                graphics.fill(
                    1, 1, 15, 15,
                    Mth.color(red * shadeColor, green * shadeColor, blue * shadeColor) | alpha << 24
                );
            }

            if (iconSkin != null) {
                graphics.setColor(shadeColor, shadeColor, shadeColor, alpha / 255f);
                PlayerFaceRenderer.draw(graphics, iconSkin.get(), 2, 2, 12);
                graphics.setColor(1f, 1f, 1f, 1f);
            }
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
