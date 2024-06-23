package io.github.gaming32.bingo;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import io.github.gaming32.bingo.data.BingoDifficulties;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.ext.CommandContextExt;
import io.github.gaming32.bingo.ext.CommandSourceStackExt;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.BingoGameMode;
import io.github.gaming32.bingo.game.InvalidGoalException;
import io.github.gaming32.bingo.network.messages.s2c.RemoveBoardPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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

    private static final SuggestionProvider<CommandSourceStack> ACTIVE_GOAL_SUGGESTOR = (context, builder) -> {
        if (Bingo.activeGame == null) {
            return builder.buildFuture();
        }
        return SharedSuggestionProvider.suggestResource(
            Arrays.stream(Bingo.activeGame.getBoard().getGoals())
                .map(ActiveGoal::goal)
                .map(BingoGoal.Holder::id),
            builder
        );
    };

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext registry,
        Commands.CommandSelection selection
    ) {
        final CommandNode<CommandSourceStack> bingoCommand = dispatcher.register(literal("bingo")
            .then(literal("start")
                .requires(source -> source.hasPermission(2))
            )
            .then(literal("stop")
                .requires(source -> source.hasPermission(2) && Bingo.activeGame != null)
                .executes(ctx -> {
                    if (Bingo.activeGame == null) {
                        throw NO_GAME_RUNNING.create();
                    }
                    Bingo.activeGame.endGame(ctx.getSource().getServer().getPlayerList());
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(literal("reset")
                .requires(source -> source.hasPermission(2))
                .executes(BingoCommand::resetGame)
            )
            .then(literal("board")
                .requires(source -> Bingo.activeGame != null)
                .executes(ctx -> {
                    if (Bingo.activeGame == null) {
                        throw NO_GAME_RUNNING.create();
                    }
                    int size = Bingo.activeGame.getBoard().getSize();

                    MenuType<?> menuType = switch (size) {
                        case 1 -> MenuType.GENERIC_9x1;
                        case 2 -> MenuType.GENERIC_9x2;
                        case 3 -> MenuType.GENERIC_9x3;
                        case 4 -> MenuType.GENERIC_9x4;
                        case 5 -> MenuType.GENERIC_9x5;
                        case 6 -> MenuType.GENERIC_9x6;
                        default -> throw CANNOT_SHOW_BOARD.create(size);
                    };

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
                                        Bingo.activeGame.getBoard().getGoal(x, y).toItemStackWithComponents()
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
                        if (Bingo.activeGame == null) {
                            throw NO_GAME_RUNNING.create();
                        }
                        ctx.getSource().sendSuccess(() -> ComponentUtils.wrapInSquareBrackets(
                            Bingo.translatable("bingo.board.copy")
                        ).withStyle(s -> s
                            .withColor(ChatFormatting.GREEN)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Bingo.translatable("chat.copy.click")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Bingo.activeGame.getBoard().toString())) // TODO: I18n?
                        ), false);
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(literal("difficulties")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> {
                        if (Bingo.activeGame == null) {
                            throw NO_GAME_RUNNING.create();
                        }
                        final BingoBoard board = Bingo.activeGame.getBoard();
                        final StringBuilder line = new StringBuilder(board.getSize());
                        for (int y = 0; y < board.getSize(); y++) {
                            for (int x = 0; x < board.getSize(); x++) {
                                line.append(board.getGoal(x, y).goal().goal().getDifficulty().difficulty().number());
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal(line.toString()), false);
                            line.setLength(0);
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(literal("goals")
                .requires(source -> source.hasPermission(2) && Bingo.activeGame != null)
                .then(argument("players", EntityArgument.players())
                    .then(literal("award")
                        .then(argument("goal", ResourceLocationArgument.id())
                            .suggests(ACTIVE_GOAL_SUGGESTOR)
                            .executes(context -> {
                                if (Bingo.activeGame == null) {
                                    throw NO_GAME_RUNNING.create();
                                }
                                final Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                                final ResourceLocation goalId = ResourceLocationArgument.getId(context, "goal");

                                int success = 0;
                                for (final ActiveGoal goal : Bingo.activeGame.getBoard().getGoals()) {
                                    if (goal.goal().id().equals(goalId)) {
                                        for (final ServerPlayer player : players) {
                                            if (Bingo.activeGame.award(player, goal)) {
                                                success++;
                                            }
                                        }
                                    }
                                }

                                final int fSuccess = success;
                                context.getSource().sendSuccess(() -> Bingo.translatable(
                                    "bingo.award.success", players.size(), fSuccess
                                ), true);
                                return success;
                            })
                        )
                    )
                    .then(literal("revoke")
                        .then(argument("goal", ResourceLocationArgument.id())
                            .suggests(ACTIVE_GOAL_SUGGESTOR)
                            .executes(context -> {
                                if (Bingo.activeGame == null) {
                                    throw NO_GAME_RUNNING.create();
                                }
                                final Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                                final ResourceLocation goalId = ResourceLocationArgument.getId(context, "goal");

                                int success = 0;
                                for (final ActiveGoal goal : Bingo.activeGame.getBoard().getGoals()) {
                                    if (goal.goal().id().equals(goalId)) {
                                        for (final ServerPlayer player : players) {
                                            if (Bingo.activeGame.revoke(player, goal)) {
                                                success++;
                                            }
                                        }
                                    }
                                }

                                final int fSuccess = success;
                                context.getSource().sendSuccess(() -> Bingo.translatable(
                                    "bingo.revoke.success", fSuccess, players.size()
                                ), true);
                                return success;
                            })
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
            dispatcher.register(literal("bingo")
                .then(literal("start")
                    .then(literal("--size")
                        .then(argument("size", IntegerArgumentType.integer(BingoBoard.MIN_SIZE, BingoBoard.MAX_SIZE))
                            .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                        )
                    )
                    .then(literal("--difficulty")
                        .then(argument("difficulty", ResourceLocationArgument.id())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                BingoDifficulty.getIds(), builder
                            ))
                            .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                        )
                    )
                    .then(literal("--seed")
                        .then(argument("seed", LongArgumentType.longArg())
                            .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                        )
                    )
                    .then(literal("--require-goal")
                        .then(argument("required_goal", ResourceLocationArgument.id())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                BingoGoal.getGoalIds(), builder
                            ))
                            .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                        )
                    )
                    .then(literal("--exclude-tag")
                        .then(argument("excluded_tag", ResourceLocationArgument.id())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                BingoTag.getTags(), builder
                            ))
                            .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                        )
                    )
                    .then(literal("--gamemode")
                        .then(argument("gamemode", StringArgumentType.word())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                BingoGameMode.GAME_MODES.keySet(), builder
                            ))
                            .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                        )
                    )
                    .then(literal("--require-client")
                        .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                    )
                    .then(literal("--persistent")
                        .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                    )
                    .then(literal("--continue-after-win")
                        .redirect(startCommand, CommandSourceStackExt.COPY_CONTEXT)
                    )
                )
            );
            CommandNode<CommandSourceStack> currentCommand = startCommand;
            for (int i = 1; i <= 32; i++) {
                final CommandNode<CommandSourceStack> subCommand = argument("team" + i, TeamArgument.team())
                    .executes(BingoCommand::startGame)
                    .build();
                currentCommand.addChild(subCommand);
                currentCommand = subCommand;
            }
        }
    }

    private static int startGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (Bingo.activeGame != null) {
            Bingo.activeGame.endGame(context.getSource().getServer().getPlayerList());
        }

        final ResourceLocation difficultyId = getArg(context, "difficulty", () -> BingoDifficulties.MEDIUM, ResourceLocationArgument::getId);
        final long seed = getArg(context, "seed", RandomSupport::generateUniqueSeed, LongArgumentType::getLong);
        final List<ResourceLocation> requiredGoalIds = getArgs(context, "required_goal", ResourceLocationArgument::getId);
        final List<ResourceLocation> excludedTagIds = getArgs(context, "excluded_tag", ResourceLocationArgument::getId);
        final int size = getArg(context, "size", () -> BingoBoard.DEFAULT_SIZE, IntegerArgumentType::getInteger);
        final String gamemodeId = getArg(context, "gamemode", () -> "standard", StringArgumentType::getString);
        final boolean requireClient = hasNode(context, "--require-client");
        final boolean persistent = hasNode(context, "--persistent");
        final boolean continueAfterWin = hasNode(context, "--continue-after-win");

        final Set<PlayerTeam> teams = new LinkedHashSet<>();
        for (int i = 1; i <= 32; i++) {
            final String argName = "team" + i;
            if (!hasArg(context, argName)) break;
            final PlayerTeam team = TeamArgument.getTeam(context, argName);
            if (!teams.add(team)) {
                throw DUPLICATE_TEAMS.create(team);
            }
        }

        final BingoDifficulty.Holder difficulty = BingoDifficulty.byId(difficultyId);
        if (difficulty == null) {
            throw UNKNOWN_DIFFICULTY.create(difficultyId);
        }

        final List<BingoGoal.Holder> requiredGoals = requiredGoalIds.stream()
            .distinct()
            .map(id -> {
                final BingoGoal.Holder goal = BingoGoal.getGoal(id);
                if (goal == null) {
                    throwInBlock(UNKNOWN_GOAL.create(id));
                }
                return goal;
            })
            .toList();

        final Set<BingoTag.Holder> excludedTags = excludedTagIds.stream()
            .distinct()
            .map(id -> {
                final BingoTag.Holder tag = BingoTag.getTag(id);
                if (tag == null) {
                    throwInBlock(UNKNOWN_TAG.create(id));
                }
                return tag;
            })
            .collect(ImmutableSet.toImmutableSet());

        final BingoGameMode gamemode = BingoGameMode.GAME_MODES.get(gamemodeId);
        if (gamemode == null) {
            throw UNKNOWN_GAMEMODE.create(gamemodeId);
        }

        final CommandSyntaxException configError = gamemode.checkAllowedConfig(
            new BingoGameMode.GameConfig(gamemode, size, teams)
        );
        if (configError != null) {
            throw configError;
        }

        final MinecraftServer server = context.getSource().getServer();
        final PlayerList playerList = server.getPlayerList();

        final BingoBoard board;
        try {
            board = BingoBoard.generate(
                size,
                difficulty.difficulty(),
                teams.size(),
                RandomSource.create(seed),
                gamemode::isGoalAllowed,
                requiredGoals,
                excludedTags,
                requireClient,
                server.reloadableRegistries().lookup()
            );
        } catch (InvalidGoalException e) {
            Bingo.LOGGER.error("Invalid goal encountered generating Bingo board", e);
            throw INVALID_GOAL.create(e);
        } catch (Exception e) {
            Bingo.LOGGER.error("Error generating Bingo board", e);
            throw FAILED_TO_START.create(e);
        }
        Bingo.LOGGER.info("Generated board (seed {}):\n{}", seed, board);

        Bingo.activeGame = new BingoGame(board, gamemode, requireClient, persistent, continueAfterWin, teams.toArray(PlayerTeam[]::new));
        Bingo.updateCommandTree(playerList);
        new ArrayList<>(playerList.getPlayers()).forEach(Bingo.activeGame::addPlayer);
        playerList.broadcastSystemMessage(Bingo.translatable("bingo.started", difficulty.getDescription()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetGame(CommandContext<CommandSourceStack> context) {
        if (Bingo.activeGame != null) {
            Bingo.activeGame.endGame(context.getSource().getServer().getPlayerList());
        }
        RemoveBoardPayload.INSTANCE.sendTo(context.getSource().getServer().getPlayerList().getPlayers());
        context.getSource().sendSuccess(() -> Bingo.translatable("bingo.reset.success"), true);
        return Command.SINGLE_SUCCESS;
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

    private static <T> T getArg(
        CommandContext<CommandSourceStack> context,
        String arg, Supplier<T> defaultValue,
        BiFunction<CommandContext<CommandSourceStack>, String, T> argGetter
    ) {
        final Set<CommandContext<?>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final Queue<CommandContext<CommandSourceStack>> toVisit = new ArrayDeque<>();
        toVisit.add(context);

        while (!toVisit.isEmpty()) {
            final CommandContext<CommandSourceStack> check = toVisit.remove();
            if (hasArg(check, arg)) {
                return argGetter.apply(check, arg);
            }
            if (check.getSource() instanceof CommandSourceStackExt ext) {
                for (final CommandContext<CommandSourceStack> extra : ext.bingo$getExtraContexts()) {
                    if (visited.add(extra)) {
                        toVisit.add(extra);
                    }
                }
            }
        }

        return defaultValue.get();
    }

    private static <T> List<T> getArgs(
        CommandContext<CommandSourceStack> context, String arg,
        BiFunction<CommandContext<CommandSourceStack>, String, T> argGetter
    ) {
        final List<T> result = new ArrayList<>();

        final Set<CommandContext<?>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final Queue<CommandContext<CommandSourceStack>> toVisit = new ArrayDeque<>();
        toVisit.add(context);

        while (!toVisit.isEmpty()) {
            final CommandContext<CommandSourceStack> check = toVisit.remove();
            if (hasArg(check, arg)) {
                result.add(argGetter.apply(check, arg));
            }
            if (check.getSource() instanceof CommandSourceStackExt ext) {
                for (final CommandContext<CommandSourceStack> extra : ext.bingo$getExtraContexts()) {
                    if (visited.add(extra)) {
                        toVisit.add(extra);
                    }
                }
            }
        }

        return result;
    }

    public static boolean hasArg(CommandContext<?> context, String name) {
        if (context instanceof CommandContextExt ext) { // false on NeoForge (for now?)
            return ext.bingo$hasArg(name);
        }
        for (final ParsedCommandNode<?> node : context.getNodes()) {
            if (node.getNode() instanceof ArgumentCommandNode<?, ?> argument && argument.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasNode(CommandContext<?> context, String name) {
        return getNode(context, name) != null;
    }

    @Nullable
    public static ParsedCommandNode<?> getNode(CommandContext<?> context, String name) {
        final Set<CommandContext<?>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final Queue<CommandContext<?>> toVisit = new ArrayDeque<>();
        toVisit.add(context);

        while (!toVisit.isEmpty()) {
            final CommandContext<?> check = toVisit.remove();
            for (final ParsedCommandNode<?> node : check.getNodes()) {
                if (node.getNode().getName().equals(name)) {
                    return node;
                }
            }
            if (check.getSource() instanceof CommandSourceStackExt ext) {
                for (final CommandContext<CommandSourceStack> extra : ext.bingo$getExtraContexts()) {
                    if (visited.add(extra)) {
                        toVisit.add(extra);
                    }
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwInBlock(Throwable t) throws T {
        throw (T)t;
    }
}
