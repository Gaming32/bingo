package io.github.gaming32.bingo.mixin.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.fabric.event.FabricEvents;
import io.github.gaming32.bingo.platform.BingoPlatform;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class MixinReloadableServerResources {
    @ModifyArg(
        method = "lambda$loadResources$2",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"
        )
    )
    private static List<PreparableReloadListener> addReloadListeners(
        List<PreparableReloadListener> listeners,
        @SuppressWarnings("LocalMayBeArgsOnly") @Local ReloadableServerResources reloadableServerResources
    ) {
        final List<PreparableReloadListener> newListeners = new ArrayList<>(listeners);
        FabricEvents.ADD_RELOAD_LISTENERS.invoker().accept(new BingoPlatform.DataReloadListenerRegistrar() {
            @Override
            public ReloadableServerResources serverResources() {
                return reloadableServerResources;
            }

            @Override
            public HolderLookup.Provider registryAccess() {
                return reloadableServerResources.registryLookup;
            }

            @Override
            public void register(ResourceLocation id, PreparableReloadListener listener, Collection<ResourceLocation> dependencies) {
                newListeners.add(bingo$wrapReloadListener(id, listener, dependencies));
            }
        });
        return newListeners;
    }

    @Unique
    private static IdentifiableResourceReloadListener bingo$wrapReloadListener(ResourceLocation id, PreparableReloadListener listener, Collection<ResourceLocation> dependencies) {
        return new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return id;
            }

            @NotNull
            @Override
            public String getName() {
                return listener.getName();
            }

            @Override
            public Collection<ResourceLocation> getFabricDependencies() {
                return dependencies;
            }

            @NotNull
            @Override
            public CompletableFuture<Void> reload(
                PreparationBarrier preparationBarrier,
                ResourceManager resourceManager,
                ProfilerFiller preparationsProfiler,
                ProfilerFiller reloadProfiler,
                Executor backgroundExecutor,
                Executor gameExecutor
            ) {
                return listener.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }
        };
    }
}
