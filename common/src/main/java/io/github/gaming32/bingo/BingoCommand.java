package io.github.gaming32.bingo;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import io.github.gaming32.bingo.commandswitch.CommandSwitch;
import io.github.gaming32.bingo.data.BingoDifficulties;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.goal.BingoGoal;
import io.github.gaming32.bingo.data.goal.GoalHolder;
import io.github.gaming32.bingo.data.goal.GoalManager;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.InvalidGoalException;
import io.github.gaming32.bingo.game.mode.BingoGameMode;
import io.github.gaming32.bingo.network.messages.s2c.RemoveBoardPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.scores.PlayerTeam;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BingoCommand {
    private static final SimpleCommandExceptionType NO_GAME_RUNNING =
        new SimpleCommandExceptionType(Bingo.translatable("bingo.no_game_running"));
    private static final DynamicCommandExceptionType CANNOT_SHOW_BOARD =
        new DynamicCommandExceptionType(size -> Bingo.translatableEscape("bingo.cannot_show_board", size));
    private static final DynamicCommandExceptionType TEAM_ALREADY_EXISTS =
        new DynamicCommandExceptionType(team -> Bingo.translatableEscape(
            "bingo.team_already_exists", ((PlayerTeam)team).getFormattedDisplayName()
        ));
    private static final DynamicCommandExceptionType DUPLICATE_TEAMS =
        new DynamicCommandExceptionType(team -> Bingo.translatableEscape("bingo.duplicate_teams", ((PlayerTeam)team).getFormattedDisplayName()));
    private static final DynamicCommandExceptionType UNKNOWN_DIFFICULTY =
        new DynamicCommandExceptionType(difficultyId -> Bingo.translatableEscape("bingo.unknown_difficulty", difficultyId));
    private static final DynamicCommandExceptionType UNKNOWN_GOAL =
        new DynamicCommandExceptionType(goalId -> Bingo.translatableEscape("bingo.unknown_goal", goalId));
    private static final DynamicCommandExceptionType UNKNOWN_TAG =
        new DynamicCommandExceptionType(tagId -> Bingo.translatableEscape("bingo.unknown_tag", tagId));
    private static final DynamicCommandExceptionType UNKNOWN_GAMEMODE =
        new DynamicCommandExceptionType(gamemodeId -> Bingo.translatableEscape("bingo.unknown_gamemode", gamemodeId));
    private static final DynamicCommandExceptionType INVALID_GOAL =
        new DynamicCommandExceptionType(e -> Bingo.translatable("bingo.start.invalid_goal").withStyle(s -> s
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.nullToEmpty(((InvalidGoalException)e).getMessage())))
        ));
    private static final DynamicCommandExceptionType FAILED_TO_START =
        new DynamicCommandExceptionType(e -> Bingo.translatable("bingo.start.failed").withStyle(s -> s
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.nullToEmpty(((Throwable)e).getMessage())))
        ));
    private static final SimpleCommandExceptionType NO_TEAMS =
        new SimpleCommandExceptionType(Bingo.translatable("bingo.no_teams"));
    private static final SimpleCommandExceptionType NOT_IN_TEAM =
        new SimpleCommandExceptionType(Bingo.translatable("bingo.not_in_team"));
    private static final SimpleCommandExceptionType FORFEIT_ALREADY_FINISHED =
        new SimpleCommandExceptionType(Bingo.translatable("bingo.forfeit.already_finished"));
    private static final DynamicCommandExceptionType TEAM_NOT_PLAYING =
        new DynamicCommandExceptionType(team -> Bingo.translatableEscape("bingo.team_not_playing", team));

    private static final SuggestionProvider<CommandSourceStack> ACTIVE_GOAL_SUGGESTOR = (context, builder) -> {
        final var game = context.getSource().getServer().bingo$getGame();
        if (game == null) {
            return builder.buildFuture();
        }
        return SharedSuggestionProvider.suggestResource(
            Arrays.stream(game.getBoard().getGoals()).map(ActiveGoal::id), builder
        );
    };

    private static final CommandSwitch<Boolean> ALLOW_NEVER_GOALS_IN_LOCKOUT = CommandSwitch.storeTrue("--allow-never-goals-in-lockout");
    private static final CommandSwitch<Boolean> REQUIRE_CLIENT = CommandSwitch.storeTrue("--require-client");
    private static final CommandSwitch<Boolean> CONTINUE_AFTER_WIN = CommandSwitch.storeTrue("--continue-after-win");
    private static final CommandSwitch<Boolean> INCLUDE_INACTIVE_TEAMS = CommandSwitch.storeTrue("--include-inactive-teams");

    private static final CommandSwitch<Integer> SIZE = CommandSwitch
        .argument("--size", IntegerArgumentType.integer(BingoBoard.MIN_SIZE, BingoBoard.MAX_SIZE))
        .build(BingoBoard.DEFAULT_SIZE);
    private static final CommandSwitch<Long> SEED = CommandSwitch
        .argument("--seed", LongArgumentType.longArg())
        .build(RandomSupport::generateUniqueSeed);
    private static final CommandSwitch<Integer> AUTO_FORFEIT_TIME = CommandSwitch
        .argument("--auto-forfeit-time", TimeArgument.time(0))
        .getter(IntegerArgumentType::getInteger)
        .build(BingoGame.DEFAULT_AUTO_FORFEIT_TICKS);
    private static final CommandSwitch<Holder.Reference<BingoDifficulty>> DIFFICULTY = CommandSwitch
        .resource("--difficulty", BingoRegistries.DIFFICULTY)
        .unknownExceptionType(UNKNOWN_DIFFICULTY)
        .build(BingoDifficulties.MEDIUM);
    private static final CommandSwitch<Holder.Reference<BingoGameMode>> GAMEMODE = CommandSwitch
        .resource("--gamemode", BingoRegistries.GAME_MODE)
        .unknownExceptionType(UNKNOWN_GAMEMODE)
        .build(BingoGameMode.STANDARD.key());

    private static final CommandSwitch<Set<GoalHolder>> REQUIRE_GOAL = CommandSwitch
        .<ResourceKey<BingoGoal>, GoalHolder>specialArgument("--require-goal", ResourceKeyArgument.key(BingoRegistries.GOAL))
        .getter((context, arg) -> {
            final var key = ResourceKeyArgument.getRegistryKey(context, arg, BingoRegistries.GOAL, INVALID_GOAL);
            final var goal = GoalManager.getGoal(key.location());
            if (goal == null) {
                throw UNKNOWN_GOAL.create(key.location());
            }
            return goal;
        })
        .buildRepeatable(LinkedHashSet::new);
    private static final CommandSwitch<HolderSet<BingoTag>> EXCLUDE_TAG = CommandSwitch
        .resource("--exclude-tag", BingoRegistries.TAG)
        .unknownExceptionType(UNKNOWN_TAG)
        .buildRepeatable();

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext buildContext,
        Commands.CommandSelection selection
    ) {
        final CommandNode<CommandSourceStack> bingoCommand = dispatcher.register(literal("bingo")
            .then(literal("start")
                .requires(source -> source.hasPermission(2))
            )
            .then(literal("stop")
                .requires(source -> source.hasPermission(2) && source.getServer().bingo$getGame() != null)
                .executes(ctx -> {
                    final var game = ctx.getSource().getServer().bingo$getGame();
                    if (game == null) {
                        throw NO_GAME_RUNNING.create();
                    }
                    game.endGame(ctx.getSource().getServer().getPlayerList());
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(literal("reset")
                .requires(source -> source.hasPermission(2))
                .executes(BingoCommand::resetGame)
            )
            .then(literal("forfeit")
                .requires(source -> source.getServer().bingo$getGame() != null)
                .executes(ctx -> forfeit(ctx.getSource()))
                .then(argument("team", TeamArgument.team())
                    .requires(source -> source.hasPermission(2) && source.getServer().bingo$getGame() != null)
                    .executes(ctx -> forfeit(ctx.getSource(), TeamArgument.getTeam(ctx, "team")))
                )
            )
            .then(literal("board")
                .requires(source -> source.getServer().bingo$getGame() != null)
                .executes(ctx -> {
                    final var game = ctx.getSource().getServer().bingo$getGame();
                    if (game == null) {
                        throw NO_GAME_RUNNING.create();
                    }
                    int size = game.getBoard().getSize();

                    MenuType<?> menuType = switch (size) {
                        case 1 -> MenuType.GENERIC_9x1;
                        case 2 -> MenuType.GENERIC_9x2;
                        case 3 -> MenuType.GENERIC_9x3;
                        case 4 -> MenuType.GENERIC_9x4;
                        case 5 -> MenuType.GENERIC_9x5;
                        case 6 -> MenuType.GENERIC_9x6;
                        default -> throw CANNOT_SHOW_BOARD.create(size);
                    };

                    final var registries = ctx.getSource().registryAccess();
                    ctx.getSource().getPlayerOrException().openMenu(new MenuProvider() {
                        @Override
                        public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                            final var menu = new ChestMenu(menuType, syncId, inventory, new SimpleContainer(9 * size), size) {
                                @Override
                                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                                    sendAllDataToRemote(); // Same as in spectator mode
                                }
                            };
                            int minX = (9 - size) / 2;
                            for (int x = 0; x < size; x++) {
                                for (int y = 0; y < size; y++) {
                                    menu.getContainer().setItem(
                                        minX + y * 9 + x,
                                        game.getBoard()
                                            .getGoal(x, y)
                                            .getFallbackWithComponents(registries)
                                    );
                                }
                            }
                            return menu;
                        }

                        @NotNull
                        @Override
                        public Component getDisplayName() {
                            return Bingo.translatable("bingo.board.title");
                        }
                    });
                    return Command.SINGLE_SUCCESS;
                })
                .then(literal("copy")
                    .executes(ctx -> {
                        final var game = ctx.getSource().getServer().bingo$getGame();
                        if (game == null) {
                            throw NO_GAME_RUNNING.create();
                        }
                        ctx.getSource().sendSuccess(() -> ComponentUtils.wrapInSquareBrackets(
                            Bingo.translatable("bingo.board.copy")
                        ).withStyle(s -> s
                            .withColor(ChatFormatting.GREEN)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Bingo.translatable("chat.copy.click")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, game.getBoard().toString())) // TODO: I18n?
                        ), false);
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(literal("difficulties")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> {
                        final var game = ctx.getSource().getServer().bingo$getGame();
                        if (game == null) {
                            throw NO_GAME_RUNNING.create();
                        }
                        final BingoBoard board = game.getBoard();
                        final StringBuilder line = new StringBuilder(board.getSize());
                        for (int y = 0; y < board.getSize(); y++) {
                            for (int x = 0; x < board.getSize(); x++) {
                                line.append(board.getGoal(x, y).difficulty().orElseThrow().value().number());
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal(line.toString()), false);
                            line.setLength(0);
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(literal("goals")
                .requires(source -> source.hasPermission(2) && source.getServer().bingo$getGame() != null)
                .then(argument("players", EntityArgument.players())
                    .then(literal("award")
                        .then(argument("goal", ResourceLocationArgument.id())
                            .suggests(ACTIVE_GOAL_SUGGESTOR)
                            .executes(ctx -> awardOrRevoke(ctx, BingoGame::award, "bingo.award.success"))
                        )
                    )
                    .then(literal("revoke")
                        .then(argument("goal", ResourceLocationArgument.id())
                            .suggests(ACTIVE_GOAL_SUGGESTOR)
                            .executes(ctx -> awardOrRevoke(ctx, BingoGame::revoke, "bingo.revoke.success"))
                        )
                    )
                )
            )
            .then(literal("teams")
                .requires(source -> source.hasPermission(2))
                .then(literal("create")
                    .then(argument("color", ColorArgument.color())
                        .suggests((context, builder) -> {
                            final ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
                            return SharedSuggestionProvider.suggest(
                                ChatFormatting.getNames(true, false)
                                    .stream()
                                    .filter(n -> scoreboard.getPlayerTeam(n) == null),
                                builder
                            );
                        })
                        .executes(context -> {
                            final ChatFormatting color = ColorArgument.getColor(context, "color");
                            final String name = color.getName();

                            final ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
                            final PlayerTeam existing = scoreboard.getPlayerTeam(name);
                            if (existing != null) {
                                throw TEAM_ALREADY_EXISTS.create(existing);
                            }

                            final PlayerTeam team = scoreboard.addPlayerTeam(name);
                            team.setColor(color);
                            team.setDisplayName(Bingo.translatable("bingo.formatting." + name));

                            context.getSource().sendSuccess(
                                () -> Bingo.translatable("bingo.created_team", team.getFormattedDisplayName()),
                                true
                            );
                            return 1;
                        })
                    )
                )
                .then(literal("randomize")
                    .executes(context -> randomizeTeams(
                        context,
                        context.getSource()
                            .getServer()
                            .getPlayerList()
                            .getPlayers()
                            .stream()
                            .filter(p -> !p.isSpectator())
                            .toList(),
                        null
                    ))
                    .then(argument("players", EntityArgument.players())
                        .executes(context -> randomizeTeams(context, EntityArgument.getPlayers(context, "players"), null))
                        .then(argument("groups", IntegerArgumentType.integer(1))
                            .executes(context -> randomizeTeams(
                                context,
                                EntityArgument.getPlayers(context, "players"),
                                IntegerArgumentType.getInteger(context, "groups")
                            ))
                        )
                    )
                )
            )
        );

        {
            final CommandNode<CommandSourceStack> startCommand = bingoCommand.getChild("start");

            ALLOW_NEVER_GOALS_IN_LOCKOUT.addTo(startCommand);
            REQUIRE_CLIENT.addTo(startCommand);
            CONTINUE_AFTER_WIN.addTo(startCommand);
            INCLUDE_INACTIVE_TEAMS.addTo(startCommand);

            SIZE.addTo(startCommand);
            SEED.addTo(startCommand);
            AUTO_FORFEIT_TIME.addTo(startCommand);
            DIFFICULTY.addTo(startCommand);
            GAMEMODE.addTo(startCommand);

            REQUIRE_GOAL.addTo(startCommand);
            EXCLUDE_TAG.addTo(startCommand);

            CommandNode<CommandSourceStack> currentCommand = startCommand;
            for (int i = 1; i <= 32; i++) {
                final var teamCount = i;
                final CommandNode<CommandSourceStack> subCommand = argument("team" + i, TeamArgument.team())
                    .executes(context -> startGame(context, teamCount))
                    .build();
                currentCommand.addChild(subCommand);
                currentCommand = subCommand;
            }
        }
    }

    private static int startGame(CommandContext<CommandSourceStack> context, int teamCount) throws CommandSyntaxException {
        final MinecraftServer server = context.getSource().getServer();
        final var playerList = server.getPlayerList();

        final var existingGame = server.bingo$getGame();
        if (existingGame != null) {
            existingGame.endGame(playerList);
        }
        final var registries = context.getSource().registryAccess();

        final var difficulty = DIFFICULTY.get(context);
        final long seed = SEED.get(context);
        final var requiredGoals = REQUIRE_GOAL.get(context);
        final var excludedTags = EXCLUDE_TAG.get(context);
        final int size = SIZE.get(context);
        final var gamemode = GAMEMODE.get(context).value();
        final boolean allowNeverGoalsInLockout = ALLOW_NEVER_GOALS_IN_LOCKOUT.get(context);
        final boolean requireClient = REQUIRE_CLIENT.get(context);
        final boolean continueAfterWin = CONTINUE_AFTER_WIN.get(context);
        final boolean includeInactiveTeams = INCLUDE_INACTIVE_TEAMS.get(context);
        final int autoForfeitTicks = AUTO_FORFEIT_TIME.get(context);

        final Set<PlayerTeam> teams = LinkedHashSet.newLinkedHashSet(teamCount);
        for (int i = 1; i <= teamCount; i++) {
            final String argName = "team" + i;
            final PlayerTeam team = TeamArgument.getTeam(context, argName);
            boolean teamActive =
                includeInactiveTeams ||
                playerList.getPlayers()
                    .stream()
                    .map(Player::getScoreboardName)
                    .anyMatch(team.getPlayers()::contains);
            if (teamActive && !teams.add(team)) {
                throw DUPLICATE_TEAMS.create(team);
            }
        }
        if (teams.isEmpty()) {
            throw NO_TEAMS.create();
        }

        final CommandSyntaxException configError = gamemode.checkAllowedConfig(
            new BingoGameMode.GameConfig(gamemode, size, teams)
        );
        if (configError != null) {
            throw configError;
        }

        final BingoBoard board;
        try {
            board = BingoBoard.generate(
                size,
                difficulty.value().number(),
                teams.size(),
                RandomSource.create(seed),
                gamemode::isGoalAllowed,
                requiredGoals,
                excludedTags,
                allowNeverGoalsInLockout,
                requireClient,
                registries
            );
        } catch (InvalidGoalException e) {
            Bingo.LOGGER.error("Invalid goal encountered generating Bingo board", e);
            throw INVALID_GOAL.create(e);
        } catch (Exception e) {
            Bingo.LOGGER.error("Error generating Bingo board", e);
            throw FAILED_TO_START.create(e);
        }
        Bingo.LOGGER.info("Generated board (seed {}):\n{}", seed, board);

        final var game = new BingoGame(board, gamemode, requireClient, continueAfterWin, autoForfeitTicks, teams.toArray(PlayerTeam[]::new));
        server.bingo$setGame(game);
        Bingo.updateCommandTree(playerList);
        new ArrayList<>(playerList.getPlayers()).forEach(game::addPlayer);
        playerList.broadcastSystemMessage(
            Bingo.translatable("bingo.started", difficulty.value().description()),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int resetGame(CommandContext<CommandSourceStack> context) {
        final var server = context.getSource().getServer();
        final var game = server.bingo$getGame();
        if (game != null) {
            game.endGame(server.getPlayerList());
        }
        RemoveBoardPayload.INSTANCE.sendTo(server.getPlayerList().getPlayers());
        context.getSource().sendSuccess(() -> Bingo.translatable("bingo.reset.success"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int forfeit(CommandSourceStack source) throws CommandSyntaxException {
        final var game = source.getServer().bingo$getGame();
        if (game == null) {
            throw NO_GAME_RUNNING.create();
        }
        ServerPlayer player = source.getPlayerOrException();
        BingoBoard.Teams team = game.getTeam(player);
        if (!team.any()) {
            throw NOT_IN_TEAM.create();
        }
        if (game.forfeit(source.getServer().getPlayerList(), team)) {
            source.sendSuccess(() -> Bingo.translatable("bingo.forfeit.success"), false);
            return Command.SINGLE_SUCCESS;
        } else {
            throw FORFEIT_ALREADY_FINISHED.create();
        }
    }

    private static int forfeit(CommandSourceStack source, PlayerTeam team) throws CommandSyntaxException {
        final var game = source.getServer().bingo$getGame();
        if (game == null) {
            throw NO_GAME_RUNNING.create();
        }
        int teamIndex = ArrayUtils.indexOf(game.getTeams(), team);
        if (teamIndex == -1) {
            throw TEAM_NOT_PLAYING.create(team.getFormattedDisplayName());
        }
        if (game.forfeit(source.getServer().getPlayerList(), BingoBoard.Teams.fromOne(teamIndex))) {
            source.sendSuccess(() -> Bingo.translatable("bingo.forfeit.success.team", team.getFormattedDisplayName()), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw FORFEIT_ALREADY_FINISHED.create();
        }
    }

    private static int awardOrRevoke(
        CommandContext<CommandSourceStack> context,
        TriFunction<BingoGame, ServerPlayer, ActiveGoal, Boolean> action,
        @Translatable String resultKey
    ) throws CommandSyntaxException {
        final var game = context.getSource().getServer().bingo$getGame();
        if (game == null) {
            throw NO_GAME_RUNNING.create();
        }
        final Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        final ResourceLocation goalId = ResourceLocationArgument.getId(context, "goal");

        int success = 0;
        for (final ActiveGoal goal : game.getBoard().getGoals()) {
            if (goal.id().equals(goalId)) {
                for (final ServerPlayer player : players) {
                    if (action.apply(game, player, goal)) {
                        success++;
                    }
                }
            }
        }

        final int fSuccess = success;
        context.getSource().sendSuccess(() -> Bingo.translatable(resultKey, players.size(), fSuccess), true);
        return success;
    }

    private static int randomizeTeams(
        CommandContext<CommandSourceStack> context,
        Collection<ServerPlayer> players,
        Integer groupCount
    ) throws CommandSyntaxException {
        final ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
        final List<PlayerTeam> teams = new ArrayList<>(scoreboard.getPlayerTeams());
        Collections.shuffle(teams);
        if (groupCount != null && groupCount < teams.size()) {
            teams.subList(groupCount, teams.size()).clear();
        }
        if (teams.isEmpty()) {
            throw NO_TEAMS.create();
        }

        final List<ServerPlayer> playerList = new ArrayList<>(players);
        Collections.shuffle(playerList);
        for (int i = 0; i < playerList.size(); i++) {
            scoreboard.addPlayerToTeam(playerList.get(i).getScoreboardName(), teams.get(i % teams.size()));
        }

        context.getSource().sendSuccess(
            () -> Bingo.translatable("bingo.added_to_teams", players.size(), Math.min(players.size(), teams.size())),
            true
        );
        return players.size();
    }
}
