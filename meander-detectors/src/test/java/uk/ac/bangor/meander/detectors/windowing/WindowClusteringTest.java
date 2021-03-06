package uk.ac.bangor.meander.detectors.windowing;

import lombok.extern.java.Log;
import org.junit.Test;
import uk.ac.bangor.meander.detectors.clusterers.Cluster;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;
import uk.ac.bangor.meander.detectors.clusterers.StreamClusterer;
import uk.ac.bangor.meander.detectors.windowing.pipes.WindowClustering;
import uk.ac.bangor.meander.detectors.windowing.support.Window;
import uk.ac.bangor.meander.detectors.windowing.support.WindowPair;

import java.util.Arrays;
import java.util.List;

/**
 * @author Will Faithfull
 */
@Log
public class WindowClusteringTest {

    @Test
    public void testClusteringWindow() {
        WindowClustering windowClustering = new WindowClustering(30, new KMeansStreamClusterer(3));

        evenDistribution(30, windowClustering);

        showClusters(windowClustering);
    }

    @Test
    public void testClusteringWindowPair() {
        WindowClustering w1 = new WindowClustering(30, new KMeansStreamClusterer(3));
        WindowClustering w2 = new WindowClustering(30, new KMeansStreamClusterer(3));

        evenDistribution(60, new WindowPair<>(w1, w2));

        showClusters(w1);
        showClusters(w2);
    }

    public void showClusters(WindowClustering windowClustering) {
        StreamClusterer clusterer = windowClustering.getClusterer();
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