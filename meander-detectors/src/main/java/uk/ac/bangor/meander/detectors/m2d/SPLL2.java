package uk.ac.bangor.meander.detectors.m2d;

import Jama.Matrix;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.Threshold;
import uk.ac.bangor.meander.detectors.clusterers.Cluster;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;
import uk.ac.bangor.meander.detectors.stats.cdf.ChiSquared;
import uk.ac.bangor.meander.detectors.windowing.WindowPairClustering;
import uk.ac.bangor.meander.detectors.windowing.support.ClusteringWindowPair;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.List;

/**
 * @author Will Faithfull
 */
public class SPLL2 {

    public static class SPLLReduction implements Pipe<ClusteringWindowPair, Double> {

        @Override
        public Double execute(ClusteringWindowPair value, StreamContext context) {
            double[] distances = distancesToW1Clusters(value.getHead().getNewest(), value);

            if(distances == null)
                return 0d;

            double likelihoodTerm = 0;
            for(int i=0;i<distances.length;i++) {
                likelihoodTerm += distances[i];
            }
            return likelihoodTerm / distances.length;
        }

        private double[] distancesToW1Clusters(Double[] example, ClusteringWindowPair clusteringWindowPair) {
            double[] distributionW1 = clusteringWindowPair.getP().getDistribution();
            double[] distributionW2 = new double[distributionW1.length];

            List<Cluster> w1Clusters = clusteringWindowPair.getP().getClusters();

            double[] clusterToExampleDistances = new double[distributionW1.length];

            Matrix pooledCovarianceW1 = new Matrix(example.length, example.length);
            for(int k=0;k<w1Clusters.size();k++) {
                Cluster cluster = w1Clusters.get(k);
                pooledCovarianceW1 = pooledCovarianceW1
                        .plus(new Matrix(cluster.getCovariance()))  // Cluster covariance
                        .times(distributionW1[k]);                  // Weighted by the priors
            }
            pooledCovarianceW1 = pooledCovarianceW1.plus(Matrix.random(example.length, example.length).times(0.0000001));

            Matrix inverseCovariance = pooledCovarianceW1.inverse();

            double minDist = Double.POSITIVE_INFINITY;
            int minDistIndex = -1;

            for (int i = 0; i < clusteringWindowPair.getQ().getDistribution().length; i++) {
                Double[] w2Example = clusteringWindowPair.getHead().getElements()[i];
                for(int k = 0; k < w1Clusters.size(); k++) {
                    Cluster cluster = w1Clusters.get(k);

                    if(cluster.isEmpty()) {
                        continue;
                    }

                    double dist = mahalanobis(
                            new Matrix(CollectionUtils.unbox(w2Example), 1),           // Each point
                            new Matrix(cluster.getCentre(), 1), // Each cluster centre
                            inverseCovariance);

                    if(dist < minDist) {
                        minDist = dist;
                        minDistIndex = k;
                    }
                }

                clusterToExampleDistances[i] = minDist;
                distributionW2[minDistIndex]++;
            }

            // Normalise distribution to 0..1
            for(int k = 0; k < w1Clusters.size(); k++) {
                distributionW2[k] /= clusteringWindowPair.getHead().size();
            }

            return clusterToExampleDistances;
        }

        private double mahalanobis(Matrix a, Matrix b, Matrix inverseCovarianceMatrix) {

            Matrix meanMinusObservation = a.minus(b);
            Matrix distance = meanMinusObservation.times(inverseCovarianceMatrix.times(meanMinusObservation.transpose()));

            double dist = Math.sqrt(Math.abs(distance.get(0,0)));
            if(Double.isNaN(dist))
                throw new RuntimeException("Distance calculation included NaN term");
            return dist;
        }

    }

    public static Pipe<Double[], Boolean> detector(int size, int K) {
        return chiSq(size,K)
                .then(Threshold.lessThan(0.05));
    }

    public static Pipe<Double[], Double> st(int size, int K) {
        return new WindowPairClustering(size, () -> new KMeansStreamClusterer(K))
                .then(new SPLLReduction());
    }

    public static Pipe<Double[], Double> chiSq(int size, int K) {
        return new WindowPairClustering(size, () -> new KMeansStreamClusterer(K))
                .then(new SPLLReduction())
                .then(new ChiSquared());
    }
}
