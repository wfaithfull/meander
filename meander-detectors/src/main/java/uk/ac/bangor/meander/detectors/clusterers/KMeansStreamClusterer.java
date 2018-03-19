package uk.ac.bangor.meander.detectors.clusterers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Will Faithfull
 */
public class KMeansStreamClusterer extends AbstractStreamClusterer implements StreamClusterer {

    int seen = 0;
    private int k;
    private Cluster[] clusters;

    public KMeansStreamClusterer(int k) {
        this.k = k;
        this.clusters = new Cluster[k];
    }

    @Override
    public int update(double[] example) {
        int assigned;
        if(seen < k) {
            // Forgy method
            clusters[seen] = new CentroidCluster(example);
            assigned = seen;
        } else {

            int cluster = minimumSquaredEuclidean(example);
            clusters[cluster].add(example);
            assigned = cluster;
        }
        seen++;

        return assigned;
    }

    @Override
    public void drop(int cluster, double[] example) {
        clusters[cluster].drop(example);
    }

    private boolean anyEmpty() {
        for(Cluster cluster : clusters) {
            if(cluster.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private int minimumSquaredEuclidean(double[] example) {

        int min = 0;
        double dist = Double.POSITIVE_INFINITY;

        for(int i=0;i<k;i++) {
            double tmp = euclidean(clusters[i].getCentre(), example);

            if(tmp < dist) {
                dist = tmp;
                min = i;
            }
        }

        return min;
    }

    private double euclidean(double[] p, double[] q) {
        double squareSum = 0;
        for(int i=0; i<p.length; i++) {
            squareSum += Math.pow((p[i] - q[i]), 2);
        }

        return Math.sqrt(squareSum);
    }

    @Override
    public List<Cluster> getClusters() {

        for(Cluster cluster : clusters) {
            if(cluster == null) {
                return Collections.emptyList();
            }
        }

        return Arrays.asList(clusters);
    }

}
