package io.github.gaming32.bingo.rating;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pocketcombats.openskill.data.MatchMakingRating;
import com.pocketcombats.openskill.data.SimpleMatchMakingRating;
import io.github.gaming32.bingo.util.Identifiers;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class BingoRatings extends SavedData {
    private static final Codec<MatchMakingRating> RATING_CODEC = RecordCodecBuilder.create(
        i -> i.group(
            Codec.DOUBLE.fieldOf("mu").forGetter(MatchMakingRating::mu),
            Codec.DOUBLE.fieldOf("sigma").forGetter(MatchMakingRating::sigma)
        ).apply(i, SimpleMatchMakingRating::new)
    );
    public static final Codec<BingoRatings> CODEC = RecordCodecBuilder.create(
        i -> i.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, RATING_CODEC).fieldOf("ratings")
                .forGetter(BingoRatings::getRatings)
        ).apply(i, BingoRatings::new)
    );
    public static final SavedDataType<BingoRatings> TYPE = new SavedDataType<>(
        Identifiers.bingo("ratings"), BingoRatings::new, CODEC, null
    );

    private final Map<UUID, MatchMakingRating> ratings;

    public BingoRatings() {
        this.ratings = new HashMap<>();
    }

    public BingoRatings(Map<UUID, MatchMakingRating> ratings) {
        this.ratings = new HashMap<>(ratings);
    }

    public Map<UUID, MatchMakingRating> getRatings() {
        return ratings;
    }

    public MatchMakingRating getRating(UUID player) {
        return ratings.getOrDefault(player, BingoRatingEngine.DEFAULT_RATING);
    }
}
