package uk.ac.bangor.meander.detectors.windowing;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Will Faithfull
 */
@Getter @AllArgsConstructor
public class DistributionPair {
    Double[] last;
    double[] p, q;
}
