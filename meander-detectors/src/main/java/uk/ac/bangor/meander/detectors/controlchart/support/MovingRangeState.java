package uk.ac.bangor.meander.detectors.controlchart.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.bangor.meander.detectors.stats.support.IncrementalStatistics;

/**
 * @author Will Faithfull
 */
@Getter
@AllArgsConstructor
public class MovingRangeState {
    double statistic, center, last;
    IncrementalStatistics statistics;
}