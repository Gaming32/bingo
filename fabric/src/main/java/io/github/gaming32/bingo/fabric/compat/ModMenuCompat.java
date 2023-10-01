package io.github.gaming32.bingo.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.gaming32.bingo.client.BingoClientConfig;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(BingoClientConfig.class, parent).get();
    }
}
