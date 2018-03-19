package uk.ac.bangor.meander.detectors.clusterers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.stat.correlation.Covariance;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.windowing.FixedWindow;
import uk.ac.bangor.meander.detectors.windowing.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Will Faithfull
 */
public class SlowApacheKMeansClusterer implements StreamClusterer {

    private final KMeansPlusPlusClusterer<DoublePoint> clusterer;
    private final Window<DoublePoint>                  window;
    private       List<CentroidCluster<DoublePoint>>   clusters;
    private       int                                  nFeatures;

    public SlowApacheKMeansClusterer(int size, int K) {
        this.clusterer = new KMeansPlusPlusClusterer<>(K, 100, new EuclideanDistance(),
                RandomGeneratorFactory.createRandomGenerator(new Random()),
                KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT);
        this.clusters = new ArrayList<>();
        this.window = new FixedWindow<>(size, DoublePoint.class);
    }

    @Override
    public int update(double[] example) {
        this.nFeatures = example.length;
        window.update(new DoublePoint(example));

        if (window.size() > 3) {
            this.clusters = this.clusterer.cluster(Arrays.asList(window.getElements()));

            for (int k = 0; k < clusters.size(); k++) {
                if (clusters.get(k).getPoints().contains(example)) {
                    return k;
                }
            }
        }

        return -1;
    }

    @Override
    public void drop(int cluster, double[] example) {

    }

    @Override
    public List<Cluster> getClusters() {
        return clusters.stream().map(ac -> new ApacheClusterAdapter(ac, nFeatures)).collect(Collectors.toList());
    }

    @Override
    public double[] getDistribution() {
        double[] distribution = new double[clusters.size()];
        int sum = 0;
        for (int k = 0; k < clusters.size(); k++) {
            distribution[k] = clusters.get(k).getPoints().size();
            sum += distribution[k];
        }

        for (int k = 0; k < clusters.size(); k++) {
            distribution[k] /= sum;
        }

        return distribution;
    }

    protected List<org.apache.commons.math3.ml.clustering.CentroidCluster<DoublePoint>> cluster(List<DoublePoint> adaptedPoints) {
        List<org.apache.commons.math3.ml.clustering.CentroidCluster<DoublePoint>> clusters = clusterer.cluster(adaptedPoints);
        return clusters;
    }

    static class ApacheClusterAdapter implements Cluster {

        private org.apache.commons.math3.ml.clustering.CentroidCluster<DoublePoint> apacheCluster;
        private int                                                                 nFeatures;

        public ApacheClusterAdapter(org.apache.commons.math3.ml.clustering.CentroidCluster<DoublePoint> apacheCluster, int nFeatures) {
            this.apacheCluster = apacheCluster;
            this.nFeatures = nFeatures;
        }

        @Override
        public int getWeight() {
            return apacheCluster.getPoints().size();
        }

        @Override
        public boolean isEmpty() {
            return apacheCluster.getPoints().isEmpty();
        }

        @Override
        public double[] getCentre() {
            return apacheCluster.getCenter().getPoint();
        }

        @Override
        public double[] getVariance() {
            throw new NotImplementedException();
        }

        @Override
        public double[] getStdDev() {
            throw new NotImplementedException();
        }

        @Override
        public double[][] getCovariance() {
            List<DoublePoint> data = apacheCluster.getPoints();

            RealMatrix clusterCovariance;

            // So, in the quite rare case where we have very little diversity in the clustering data, the clusterer
            // can fail to correctly follow the empty cluster strategy, and we end up with an empty cluster out here.
            // It makes very little difference to us, be it empty cluster or singleton cluster, we will just initialise
            // an empty covariance matrix, because we can't calculate a covariance matrix for either.
            if (getWeight() <= 1) {
                clusterCovariance = new Array2DRowRealMatrix(nFeatures, nFeatures);
            } else {
                double[][] clusterData = CollectionUtils.toArray(data.stream().map(x -> x.getPoint()).collect(Collectors.toList()));
                clusterCovariance = new Covariance(clusterData).getCovarianceMatrix();
            }

            return clusterCovariance.getData();
        }

        @Override
        public void add(double[] example) {
            apacheCluster.addPoint(new DoublePoint(example));
        }

        @Override
        public void drop(double[] example) {
            apacheCluster.getPoints().remove(example);
        }
    }
}
