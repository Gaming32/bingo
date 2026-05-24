package io.github.gaming32.bingo.rating;

import com.pocketcombats.openskill.Adjudicator;
import com.pocketcombats.openskill.QualityEvaluator;
import com.pocketcombats.openskill.RatingModelConfig;
import com.pocketcombats.openskill.aggregate.DefaultTeamRatingAggregator;
import com.pocketcombats.openskill.aggregate.TeamRatingAggregator;
import com.pocketcombats.openskill.data.MatchMakingRating;
import com.pocketcombats.openskill.data.SimpleMatchMakingRating;
import com.pocketcombats.openskill.model.PlackettLuce;
import com.pocketcombats.openskill.model.RatingModel;
import java.util.UUID;

public final class BingoRatingEngine {
    private static final RatingModelConfig CONFIG = RatingModelConfig.builder()
        .setZ(3.0)
        .setAlpha(25.0)
        .setTarget(1200.0)
        .build();
    private static final RatingModel MODEL = new PlackettLuce(CONFIG);

    public static final Adjudicator<UUID> ADJUDICATOR = new Adjudicator<>(CONFIG, MODEL);
    public static final TeamRatingAggregator AGGREGATOR = new DefaultTeamRatingAggregator(CONFIG);
    public static final QualityEvaluator QUALITY_EVALUATOR = new QualityEvaluator(CONFIG);
    public static final MatchMakingRating DEFAULT_RATING = new SimpleMatchMakingRating(25.0, 25.0 / 3.0);

    private BingoRatingEngine() {
    }

    public static double bingoRating(MatchMakingRating rating) {
        return ordinalToBingoRating(ordinal(rating));
    }

    private static double ordinal(MatchMakingRating rating) {
        return rating.mu() - CONFIG.balance().z() * rating.sigma();
    }

    private static double ordinalToBingoRating(double ordinal) {
        return CONFIG.balance().target() + ordinal * CONFIG.balance().alpha();
    }
}
