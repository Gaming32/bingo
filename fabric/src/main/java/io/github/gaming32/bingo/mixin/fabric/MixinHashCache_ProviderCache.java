package io.github.gaming32.bingo.mixin.fabric;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashCode;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.BufferedWriter;
import java.nio.file.Path;
import java.util.Map;

@Mixin(targets = "net.minecraft.data.HashCache$ProviderCache")
public class MixinHashCache_ProviderCache {
    @WrapOperation(
        method = "save",
        at = @At(
            value = "INVOKE",
            target = "Ljava/io/BufferedWriter;write(Ljava/lang/String;)V"
        )
    )
    private void skipDate(BufferedWriter instance, String str, Operation<Void> original, @Local String date) {
        //noinspection StringEquality
        if (str != date) {
            original.call(instance, str);
        }
    }

    @WrapOperation(
        method = "save",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableMap;entrySet()Lcom/google/common/collect/ImmutableSet;"
        )
    )
    private ImmutableSet<Map.Entry<Path, HashCode>> standardizeOrder(
        ImmutableMap<Path, HashCode> instance,
        Operation<ImmutableSet<Map.Entry<Path, HashCode>>> original
    ) {
        return original.call(instance)
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(ImmutableSet.toImmutableSet());
    }

    @WrapOperation(
        method = "save",
        at = @At(
            value = "INVOKE",
            target = "Ljava/nio/file/Path;toString()Ljava/lang/String;"
        )
    )
    private String standardizeFilePaths(Path instance, Operation<String> original) {
        return original.call(instance).replace('\\', '/');
    }
}
