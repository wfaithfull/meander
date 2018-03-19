package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.stat.correlation.Covariance;
import uk.ac.bangor.meander.detectors.windowing.ClusteringWindowPairPipe;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Will Faithfull
 *
 * The base abstraction for the log likelihood detectors.
 *
 * Quantizes the input space into the distribution of observations with regards to K-means cluster membership.
 *
 * Delegates the final decision for change to the implementing subclass, which is presented with a pair of discrete
 * probability distributions about which to reason.
 *
 * This uses the Apache commons KMeansPlusPlusClusterer for clustering, although in theory it should be straightforward
 * to substitute this for another clustering algorithm.
 *
 * ** Deprecated because this is super slow compared to stream clustering. **
 *
 * Use {@link ClusteringWindowPairPipe} instead which uses the baked in stream clustering framework
 * for orders of magnitude speed increase.
 */
@Deprecated
public abstract class AbstractKMeansQuantizingDetector {

    protected final WindowPair<double[]>                 windowPair;
    private final   KMeansPlusPlusClusterer<DoublePoint> clusterer;
    protected final int                                  K;

    @Getter(AccessLevel.PROTECTED)
    private List<Double> classPriors;
    @Getter(AccessLevel.PROTECTED)
    private List<double[]> clusterMeans;
    @Getter(AccessLevel.PROTECTED)
    private double[] minClusterToObservationDistances;

    private List<RealMatrix> priorWeightedClusterCovariance;

    @Getter(AccessLevel.PROTECTED)
    private final int nObservations;

    @Getter(AccessLevel.PROTECTED)
    private int nFeatures;

    private double[][] covariance;

    protected double[] p1;
    protected double[] p2;

    public AbstractKMeansQuantizingDetector(WindowPair<double[]> windowPair) {
        this(windowPair, 3);
    }

    public AbstractKMeansQuantizingDetector(WindowPair<double[]> windowPair, int K) {
        this.windowPair = windowPair;
        this.K = K;
        clusterer = new KMeansPlusPlusClusterer<>(K, 100, new EuclideanDistance(), RandomGeneratorFactory.createRandomGenerator(new Random()), KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT);
        nObservations = windowPair.getTail().capacity();
    }

    protected List<CentroidCluster<DoublePoint>> cluster(double[][] w1) {
        List<DoublePoint> adaptedPoints = new ArrayList<>();
        for(double[] point : w1) {
            adaptedPoints.add(new DoublePoint(point));
        }

        List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(adaptedPoints);

        classPriors = new ArrayList<>();
        clusterMeans = new ArrayList<>();
        priorWeightedClusterCovariance = new ArrayList<>();

        int totalObservations   = w1.length;
        int nFeatures           = w1[0].length;

        covariance = new Covariance(w1).getCovarianceMatrix().getData();

        // Calculate the REFERENCE distribution from Window 1
        for(CentroidCluster<DoublePoint> cluster : clusters) {
            double nObservations       = cluster.getPoints().size();
            double[] center         = cluster.getCenter().getPoint();

            clusterMeans.add(center);
            double prior = nObservations / totalObservations;
            classPriors.add(prior);

            List<DoublePoint> data = cluster.getPoints();

            RealMatrix clusterCovariance;

            // So, in the quite rare case where we have very little diversity in the clustering data, the clusterer
            // can fail to correctly follow the empty cluster strategy, and we end up with an empty cluster out here.
            // It makes very little difference to us, be it empty cluster or singleton cluster, we will just initialise
            // an empty covariance matrix, because we can't calculate a covariance matrix for either.
            if(nObservations <= 1.0) {
                clusterCovariance = new Array2DRowRealMatrix(nFeatures, nFeatures);
            } else {
                double[][] clusterData = CollectionUtils.toArray(data.stream().map(x -> x.getPoint()).collect(Collectors.toList()));
                clusterCovariance = new Covariance(clusterData).getCovarianceMatrix();
            }

            clusterCovariance = clusterCovariance.scalarMultiply(prior);
            priorWeightedClusterCovariance.add(clusterCovariance);
        }
        return clusters;
    }

    ThreadLocalRandom random = ThreadLocalRandom.current();

    public void update(Double[] input) {
        double[] unboxed = new double[input.length];
        for(int i=0;i<input.length;i++) {
            unboxed[i] = input[i];
        }
        update(unboxed);
    }

    public void update(double[] input) {
        windowPair.update(input);

        if(windowPair.size() != windowPair.capacity())
            return;

        double[][] window1 = windowPair.getTail().getElements();
        double[][] window2 = windowPair.getHead().getElements();

        this.nFeatures = window1[0].length;

        List<CentroidCluster<DoublePoint>> clusters = cluster(window1);
        double totalObservations = (double)window1.length;

        /**
         * Calculate the reference distribution (P1) from window 1, i.e. the population of the K clusters.
         */
        p1 = clusters.stream().mapToDouble(c -> c.getPoints().size() / totalObservations).toArray();
        p2 = new double[clusters.size()];
        minClusterToObservationDistances = new double[(int)totalObservations];

        RealMatrix finalCovariance = new Array2DRowRealMatrix(nFeatures, nFeatures);
        for(RealMatrix covarianceMatrix : priorWeightedClusterCovariance) {
            finalCovariance = finalCovariance.add(covarianceMatrix);
        }

        SingularValueDecomposition decomposition = new SingularValueDecomposition(finalCovariance);
        RealMatrix inverseCovariance;
        if(decomposition.getSolver().isNonSingular())
            inverseCovariance = decomposition.getSolver().getInverse();
        else {
            double[][] finalCov = finalCovariance.getData();
            inverseCovariance = new Array2DRowRealMatrix(nFeatures, nFeatures);
            for(int m=0;m<finalCov.length;m++) {
                for(int n = 0; n<finalCov[m].length; n++) {
                    double covMN = finalCov[m][n];
                    if(covMN <= 0.0001d) {
                        finalCov[m][n] += 0.0001 * random.nextDouble();
                    }
                    inverseCovariance.setEntry(m,n, 1/finalCov[m][n]);
                }
            }
        }

        /**
         * Calculate the new distribution (P2) - do the points in window 2 get distributed into roughly the same clusters as
         * we see in the window 1 clustering? If not, we may need to signal change.
         */
        double w2Observations = window2.length;
        for(int i=0;i<w2Observations;i++) {
            double[] observation = window2[i];
            double minDist = Double.POSITIVE_INFINITY;
            int minDistIndex = -1;

            RealMatrix observationVector = MatrixUtils.createRowRealMatrix(observation);

            for(int k=0;k<clusters.size();k++) {
                RealMatrix clusterCenterVector = MatrixUtils.createRowRealMatrix(clusterMeans.get(k));

                double dist = mahalanobisDistance(clusterCenterVector, observationVector, inverseCovariance);

                if(dist < minDist) {
                    minDist = dist;
                    minDistIndex = k;
                }
            }

            minClusterToObservationDistances[i] = minDist;
            p2[minDistIndex]++;
        }

        for(int i=0;i<K;i++) {
            p2[i] /= w2Observations;
        }
    }

    private static double min(RealMatrix matrix) {
        double min = Double.POSITIVE_INFINITY;
        for (double[] row : matrix.getData()) {
            for(double col : row) {
                if(col < min) {
                    min = col;
                }
            }
        }
        return min;
    }

    private double mahalanobisDistance(RealMatrix a, RealMatrix b, RealMatrix inverseCovarianceMatrix) {

        RealMatrix meanMinusObservation = a.subtract(b);
        RealMatrix distance = meanMinusObservation.multiply(inverseCovarianceMatrix.multiply(meanMinusObservation.transpose()));

        double dist = Math.sqrt(Math.abs(distance.getEntry(0,0)));
        if(Double.isNaN(dist))
            throw new RuntimeException("Distance calculation included NaN term");
        return dist;
    }
}
