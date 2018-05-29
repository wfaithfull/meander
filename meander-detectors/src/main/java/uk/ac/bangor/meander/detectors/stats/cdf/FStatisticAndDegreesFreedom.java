package uk.ac.bangor.meander.detectors.stats.cdf;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Will Faithfull
 */
@Getter
@AllArgsConstructor
public class FStatisticAndDegreesFreedom {
    private int df1, df2;
    private double statistic;
}
