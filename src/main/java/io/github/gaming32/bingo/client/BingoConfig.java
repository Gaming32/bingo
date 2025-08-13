package io.github.gaming32.bingo.client;

import com.electronwill.nightconfig.core.file.FileConfig;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BingoConfig {
    private final FileConfig config;

    private Set<UUID> nerfedPlayers = new HashSet<>();

    public BingoConfig(Path configPath) {
        config = FileConfig.of(configPath).checked();
    }

    public void load() {
        config.load();
        nerfedPlayers = deserializeNerfedPlayers(config.get("nerfed_players"));
    }

    public void save() {
        config.clear();
        config.set("nerfed_players", serializeNerfedPlayers(nerfedPlayers));
        config.save();
    }

    public Set<UUID> getNerfedPlayers() {
        return nerfedPlayers;
    }

    private static Set<UUID> deserializeNerfedPlayers(Object nerfedPlayers) {
        if (!(nerfedPlayers instanceof List<?> list)) {
            return new HashSet<>();
        }

        Set<UUID> result = HashSet.newHashSet(list.size());

        for (Object player : list) {
            if (!(player instanceof String uuidStr)) {
                return new HashSet<>();
            }
            try {
                result.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                return new HashSet<>();
            }
        }

        return result;
    }

    private static List<String> serializeNerfedPlayers(Set<UUID> nerfedPlayers) {
        return nerfedPlayers.stream().map(UUID::toString).toList();
    }
}
