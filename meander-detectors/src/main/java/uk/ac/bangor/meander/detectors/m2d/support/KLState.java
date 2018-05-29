package uk.ac.bangor.meander.detectors.m2d.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Will Faithfull
 */
@Getter
@AllArgsConstructor
public class KLState {
    private double   statistic;
    private int      K;
    private double[] p;
    private double[] q;
}
