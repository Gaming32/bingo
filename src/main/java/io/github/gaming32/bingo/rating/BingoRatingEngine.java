package io.github.gaming32.bingo.rating;

import com.pocketcombats.openskill.Adjudicator;
import com.pocketcombats.openskill.QualityEvaluator;
import com.pocketcombats.openskill.RatingModelConfig;
import com.pocketcombats.openskill.aggregate.DefaultTeamRatingAggregator;
import com.pocketcombats.openskill.aggregate.TeamRatingAggregator;
import com.pocketcombats.openskill.data.MatchMakingRating;
import com.pocketcombats.openskill.data.SimpleMatchMakingRating;
import com.pocketcombats.openskill.math.Gaussian;
import com.pocketcombats.openskill.model.PlackettLuce;
import com.pocketcombats.openskill.model.RatingModel;
import io.github.gaming32.bingo.util.BingoUtil;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.List;
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

    // https://github.com/vivekjoshy/openskill.py/blob/65a675d21d9a0d5405731385e83be9aa781537ec/openskill/models/weng_lin/plackett_luce.py#L964
    public static double predictDraw(List<List<MatchMakingRating>> teams) {
        final var totalPlayerCount = teams.stream().mapToInt(List::size).sum();
        final var drawProbability = 1.0 / totalPlayerCount;
        final var drawMargin =
            Math.sqrt(totalPlayerCount)
            * CONFIG.beta()
            * inverseCdf((1.0 + drawProbability) / 2.0);

        record TeamRating(double mu, double sigmaSquared) {
        }
        final var teamRatings = teams.stream()
            .map(team -> {
                var mu = 0.0;
                var sigmaSquared = 0.0;
                for (final var rate : team) {
                    mu += rate.mu();
                    sigmaSquared += rate.sigma() * rate.sigma();
                }
                return new TeamRating(mu, sigmaSquared);
            })
            .toList();

        final var pairwiseProbabilities = new DoubleArrayList(BingoUtil.fullMesh(teams.size()));
        for (int i = 0; i < teams.size() - 1; i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                final var pairA = teamRatings.get(i);
                final var pairB = teamRatings.get(j);
                final var root = Math.sqrt(2 * CONFIG.beta() * CONFIG.beta() + pairA.sigmaSquared + pairB.sigmaSquared);
                pairwiseProbabilities.add(
                    Gaussian.cdf((drawMargin - pairA.mu + pairB.mu) / root)
                    - Gaussian.cdf((pairB.mu - pairA.mu - drawMargin) / root)
                );
            }
        }
        return pairwiseProbabilities.doubleStream().average().orElse(1.0);
    }

    // https://github.com/python/cpython/blob/09ff114e8c6a532ca6025450141c47df4db12f32/Lib/statistics.py#L1797
    private static double inverseCdf(double p) {
        final var mu = 0.0;
        final var sigma = 1.0;

        final var q = p - 0.5;

        if (Math.abs(q) <= 0.425) {
            final var r = 0.180625 - q * q;
            final var num = (((((((2.50908_09287_30122_6727e+3 * r +
                                   3.34305_75583_58812_8105e+4) * r +
                                  6.72657_70927_00870_0853e+4) * r +
                                 4.59219_53931_54987_1457e+4) * r +
                                1.37316_93765_50946_1125e+4) * r +
                               1.97159_09503_06551_4427e+3) * r +
                              1.33141_66789_17843_7745e+2) * r +
                             3.38713_28727_96366_6080e+0) * q;
            final var den = (((((((5.22649_52788_52854_5610e+3 * r +
                                   2.87290_85735_72194_2674e+4) * r +
                                  3.93078_95800_09271_0610e+4) * r +
                                 2.12137_94301_58659_5867e+4) * r +
                                5.39419_60214_24751_1077e+3) * r +
                               6.87187_00749_20579_0830e+2) * r +
                              4.23133_30701_60091_1252e+1) * r +
                             1.0);
            final var x = num / den;
            return mu + x * sigma;
        }

        final double num, den;
        var r = q <= 0.0 ? p : 1.0 - p;
        r = Math.sqrt(-Math.log(r));
        if (r <= 5.0) {
            r = r - 1.6;
            num = (((((((7.74545_01427_83414_07640e-4 * r +
                         2.27238_44989_26918_45833e-2) * r +
                        2.41780_72517_74506_11770e-1) * r +
                       1.27045_82524_52368_38258e+0) * r +
                      3.64784_83247_63204_60504e+0) * r +
                     5.76949_72214_60691_40550e+0) * r +
                    4.63033_78461_56545_29590e+0) * r +
                   1.42343_71107_49683_57734e+0);
            den = (((((((1.05075_00716_44416_84324e-9 * r +
                         5.47593_80849_95344_94600e-4) * r +
                        1.51986_66563_61645_71966e-2) * r +
                       1.48103_97642_74800_74590e-1) * r +
                      6.89767_33498_51000_04550e-1) * r +
                     1.67638_48301_83803_84940e+0) * r +
                    2.05319_16266_37758_82187e+0) * r +
                   1.0);
        } else {
            r = r - 5.0;
            num = (((((((2.01033_43992_92288_13265e-7 * r +
                         2.71155_55687_43487_57815e-5) * r +
                        1.24266_09473_88078_43860e-3) * r +
                       2.65321_89526_57612_30930e-2) * r +
                      2.96560_57182_85048_91230e-1) * r +
                     1.78482_65399_17291_33580e+0) * r +
                    5.46378_49111_64114_36990e+0) * r +
                   6.65790_46435_01103_77720e+0);
            den = (((((((2.04426_31033_89939_78564e-15 * r +
                         1.42151_17583_16445_88870e-7) * r +
                        1.84631_83175_10054_68180e-5) * r +
                       7.86869_13114_56132_59100e-4) * r +
                      1.48753_61290_85061_48525e-2) * r +
                     1.36929_88092_27358_05310e-1) * r +
                    5.99832_20655_58879_37690e-1) * r +
                   1.0);
        }

        var x = num / den;
        if (q < 0.0) {
            x = -x;
        }

        return mu + x * sigma;
    }
}
