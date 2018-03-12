package uk.ac.bangor.meander.detectors.windowing;

import lombok.extern.java.Log;
import org.junit.Test;
import uk.ac.bangor.meander.detectors.clusterers.Cluster;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;
import uk.ac.bangor.meander.detectors.clusterers.StreamClusterer;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Will Faithfull
 */
@Log
public class ClusteringWindowTest {

    @Test
    public void testClusteringWindow() {
        ClusteringWindow clusteringWindow = new ClusteringWindow(30, new KMeansStreamClusterer(3));

        evenDistribution(30, clusteringWindow);

        showClusters(clusteringWindow);
    }

    @Test
    public void testClusteringWindowPair() {
        ClusteringWindow w1 = new ClusteringWindow(30, new KMeansStreamClusterer(3));
        ClusteringWindow w2 = new ClusteringWindow(30, new KMeansStreamClusterer(3));

        evenDistribution(60, new WindowPair<>(w1, w2));

        showClusters(w1);
        showClusters(w2);
    }

    public void showClusters(ClusteringWindow clusteringWindow) {
        StreamClusterer clusterer = clusteringWindow.getClusterer();
        List<Cluster> clusterList = clusterer.getClusters();
        for(Cluster cluster : clusterList) {
            log.info(String.format("Cluster %s - population %d", Arrays.toString(cluster.getCentre()), cluster.getWeight()));
        }

        log.info("Distribution: " + Arrays.toString(clusterer.getDistribution()));
    }


    private void evenDistribution(int n, Window<Double[]> window) {

        int clIdx = 0;

        for(int i=0;i<n;i++) {

            if(clIdx == 0) {
                window.update(new Double[] { 1.0, 0.0, 0.0 });
            } else if (clIdx == 1) {
                window.update(new Double[] { 0.0, 1.0, 0.0 });
            } else {
                window.update(new Double[] { 0.0, 0.0, 1.0 });
            }

            clIdx++;
            if(clIdx > 2)
                clIdx = 0;
        }
    }

}