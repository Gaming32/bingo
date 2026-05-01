package io.github.gaming32.bingo.datagen;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class BingoCommandDumper implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public BingoCommandDumper(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        final Path result = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("bingo_command.json");
        return registries.thenCompose(provider -> {
            final CommandDispatcher<CommandSourceStack> commandDispatcher =
                new Commands(Commands.CommandSelection.ALL, Commands.createValidationContext(provider)).getDispatcher();
            return DataProvider.saveStable(output, ArgumentUtils.serializeNodeToJson(
                commandDispatcher, commandDispatcher.getRoot().getChild("bingo")
            ), result);
        });
    }

    @NotNull
    @Override
    public String getName() {
        return "bingo Command";
    }
}
