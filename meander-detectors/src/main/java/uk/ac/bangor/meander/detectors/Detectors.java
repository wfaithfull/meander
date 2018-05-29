package uk.ac.bangor.meander.detectors;

import moa.classifiers.core.driftdetection.*;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;
import uk.ac.bangor.meander.detectors.clusterers.SlowApacheKMeansClusterer;
import uk.ac.bangor.meander.detectors.controlchart.MR;
import uk.ac.bangor.meander.detectors.m2d.Hotelling;
import uk.ac.bangor.meander.detectors.m2d.KL;
import uk.ac.bangor.meander.detectors.m2d.SPLL;
import uk.ac.bangor.meander.detectors.m2d.SPLL2;
import uk.ac.bangor.meander.detectors.stats.cdf.ChiSquared;
import uk.ac.bangor.meander.detectors.stats.cdf.FWithDF;
import uk.ac.bangor.meander.detectors.windowing.WindowPairClustering;
import uk.ac.bangor.meander.detectors.windowing.WindowPairPipe;
import uk.ac.bangor.meander.detectors.windowing.support.ClusteringWindowPair;
import uk.ac.bangor.meander.detectors.windowing.support.WindowPair;

/**
 * @author Will Faithfull
 */
public class Detectors {

    public static class Multivariate {

        public static Pipe<Double[], Boolean> klDetector(int size, int K) {
            return new WindowPairClustering(size, () -> new KMeansStreamClusterer(K))
                    .then(new ClusteringWindowPair.Distribution())
                    .then(new KL.KLReduction())
                    .then(new Threshold<>(Threshold.Op.GT, new KL.LikelihoodRatioThreshold(), new KL.KLStateStatistic()));
        }

        public static Pipe<Double[], Boolean> klDetector(int size, int K, ChartReporter reporter) {
            return new WindowPairClustering(size, () -> new KMeansStreamClusterer(K))
                    .then(new ClusteringWindowPair.Distribution())
                    .then(new KL.KLReduction())
                    .then(
                            new Threshold<>(Threshold.Op.GT, new KL.LikelihoodRatioThreshold(), new KL.KLStateStatistic())
                                    .report(reporter::ucl, reporter::statistic)
                    );
        }

        public static Pipe<Double[], Double> klReduction(int size, int K) {
            return new WindowPairClustering(size, () -> new KMeansStreamClusterer(K))
                    .then(new ClusteringWindowPair.Distribution())
                    .then(new KL.KLReduction())
                    .then((value, context) -> value.getStatistic());
        }

        public static Pipe<Double[], Boolean> hotellingDetector(int size) {
            return new WindowPairPipe(size)
                    .then(new Hotelling.TsqReduction())
                    .then(new FWithDF())
                    .then(Threshold.lessThan(0.05));
        }

        public static Pipe<Double[], Boolean> hotellingDetector(int size, ChartReporter reporter) {
            return new WindowPairPipe(size)
                    .then(new Hotelling.TsqReduction())
                    .then(new FWithDF().complementary())
                    // Threshold inverts the cumulative probability
                    .then(Threshold.lessThan(0.05)
                            .report(reporter::lcl, reporter::statistic));
        }

        public static Pipe<Double[], Double> hotellingReduction(int size) {
            return new WindowPairPipe(size)
                    .then(new Hotelling.TsqReduction())
                    .then((value, context) -> value.getStatistic());
        }

        public static Pipe<Double[], Double> hotellingFCDF(int size) {
            return new WindowPairPipe(size)
                    .then(new Hotelling.TsqReduction())
                    .then(new FWithDF());
        }

        public static Pipe<Double[], Boolean> spllDetector(int W, int K) {
            return spllC2CDF(W, K)
                    .then(Threshold.lessThan(0.05));
        }

        public static Pipe<Double[], Boolean> spllDetector(int W, int K, ChartReporter reporter) {
            return new SPLL(new WindowPair<>(W, W, double[].class), K)
                    // If the data in W2 was generated by process P1, then the average of the squared mahalanobis distances
                    // between the W2 observations and the cluster means should be distributed according to a chi-squared
                    // distribution with n degrees of freedom, where n is the dimensionality of the feature space.
                    .then(new ChiSquared())
                    .then(
                            new Threshold<Double>(Threshold.Op.LT, 0.05,
                                    (pst, ctx) -> Math.min(pst, 1d - pst))
                                    .report(reporter::lcl, reporter::statistic)
                    );
        }

        public static Pipe<Double[], Double> spllReduction(int W, int K) {
            return new SPLL(new WindowPair<>(W, W, double[].class), K);
        }

        public static Pipe<Double[], Double> spllC2CDF(int W, int K) {
            return new SPLL(new WindowPair<>(W, W, double[].class), K)
                    .then(new ChiSquared());
        }

        public static Pipe<Double[], Boolean> spll2Detector(int W, int K, ChartReporter reporter) {
            return new WindowPairClustering(W, () -> new SlowApacheKMeansClusterer(W, K))
                    .then(new SPLL2.SPLLReduction())
                    .then(new ChiSquared())
                    .then(
                            new Threshold<Double>(Threshold.Op.LT, 0.05,
                                    (pst, ctx) -> Math.min(pst, 1d - pst))
                                    .report(reporter::lcl, reporter::statistic)
                    );
        }

        public static Pipe<Double[], Boolean> spll2Detector(int W, int K) {
            return new WindowPairClustering(W, () -> new SlowApacheKMeansClusterer(W, K))
                    .then(new SPLL2.SPLLReduction())
                    .then(new ChiSquared())
                    .then(
                            new Threshold<Double>(Threshold.Op.LT, 0.05,
                                    (pst, ctx) -> Math.min(pst, 1d - pst))
                    );
        }

    }

    public static class Univariate {

        public static Pipe<Double, Boolean> movingRangeChart() {
            return new MR.MRReduction()
                    .then(new MR.MRThreshold());
        }

        public static Pipe<Double, Boolean> movingRangeChart(ChartReporter reporter) {
            return new MR.MRReduction()
                    .then(new ReportPipe<>(reporter::statistic, MR.MRState::getStatistic))
                    .then(new MR.MRThreshold().reportThreshold(reporter::ucl));
        }

        public static Pipe<Double, Boolean> cusum() {
            return new MoaDetectorAdapter(new CusumDM());
        }

        public static Pipe<Double, Boolean> adwin() {
            return new MoaDetectorAdapter(new ADWINChangeDetector());
        }

        public static Pipe<Double, Boolean> geometricMovingAverage() {
            return new MoaDetectorAdapter(new GeometricMovingAverageDM());
        }

        public static Pipe<Double, Boolean> ddm() {
            return new MoaDetectorAdapter(new DDM());
        }

        public static Pipe<Double, Boolean> eddm() {
            return new MoaDetectorAdapter(new EDDM());
        }

        public static Pipe<Double, Boolean> ewma() {
            return new MoaDetectorAdapter(new EWMAChartDM());
        }

        public static Pipe<Double, Boolean> pageHinkley() {
            return new MoaDetectorAdapter(new PageHinkleyDM());
        }

        public static Pipe<Double, Boolean> hddmA() {
            return new MoaDetectorAdapter(new HDDM_A_Test());
        }

        public static Pipe<Double, Boolean> hddmW() {
            return new MoaDetectorAdapter(new HDDM_W_Test());
        }

        public static Pipe<Double, Boolean> seed() {
            return new MoaDetectorAdapter(new SEEDChangeDetector());
        }

        public static Pipe<Double, Boolean> seq1() {
            return new MoaDetectorAdapter(new SeqDrift1ChangeDetector());
        }

        public static Pipe<Double, Boolean> seq2() {
            return new MoaDetectorAdapter(new SeqDrift2ChangeDetector());
        }

    }
}
