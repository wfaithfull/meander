package uk.ac.bangor.meander.evaluators;

import uk.ac.bangor.meander.detectors.Detector;
import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.streams.Example;

import java.util.stream.Stream;

/**
 * @author Will Faithfull
 *
 * Performs an evaluation of the primary change detection metrics:
 *      Average Run Length      (ARL)
 *      Time to Detection       (TTD)
 *      False Alarm Rate        (FAR)
 *      Missed Detection Ratio  (MDR)
 */
public interface Evaluator {

    /**
     * Evaluate {@code detector} over the specified {@code changeStream}.
     * @param detector Multivariate change detector.
     * @param changeStream Multivariate change stream.
     * @return
     */
    Evaluation evaluate(Detector<Double[]> detector, Stream<Example> changeStream);

    /**
     * Evaluate {@code detector} over the specified {@code changeStream}.
     * @param detector Multivariate change detector.
     * @param changeStream Multivariate change stream.
     * @return
     */
    Evaluation evaluate(Detector<Double[]> detector, ChangeStreamBuilder changeStream, long limit, long n);

}
