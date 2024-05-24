package io.github.gaming32.bingo.mixin.common.client;

import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.TeamSelectionSpectatorCategory;
import net.minecraft.client.gui.spectator.RootSpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RootSpectatorMenuCategory.class)
public class MixinRootSpectatorMenuCategory {
    @Shadow @Final private List<SpectatorMenuItem> items;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addBingoTeamSelector(CallbackInfo ci) {
        if (BingoClient.clientGame != null) {
            items.add(new TeamSelectionSpectatorCategory());
        }
    }
}
