package uk.ac.bangor.meander.detectors.clusterers;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Will Faithfull
 */
@Log
public class KMeansStreamClustererTest {

    @Test
    public void testClustering() {
        KMeansStreamClusterer streamClusterer = new KMeansStreamClusterer(3);

        double[] c1 = new double[] {1.0, 0.0, 0.0};
        double[] c2 = new double[] {0.0, 1.0, 0.0};
        double[] c3 = new double[] {0.0, 0.0, 1.0};

        streamClusterer.update(c1);
        streamClusterer.update(c2);
        streamClusterer.update(c3);

        List<Cluster> clusterList = streamClusterer.getClusters();

        Assert.assertEquals(clusterList.get(0).getWeight(), 1);
        Assert.assertEquals(clusterList.get(1).getWeight(), 1);
        Assert.assertEquals(clusterList.get(2).getWeight(), 1);

        Assert.assertTrue(Arrays.equals(clusterList.get(0).getCentre(), c1));
        Assert.assertTrue(Arrays.equals(clusterList.get(1).getCentre(), c2));
        Assert.assertTrue(Arrays.equals(clusterList.get(2).getCentre(), c3));


        for(int i=0;i<6;i++) {
            streamClusterer.update(c3);
        }

        clusterList = streamClusterer.getClusters();

        Assert.assertEquals(clusterList.get(0).getWeight(), 1);
        Assert.assertEquals(clusterList.get(1).getWeight(), 1);
        Assert.assertEquals(clusterList.get(2).getWeight(), 7);

        for(Cluster cluster : clusterList) {
            log.info(String.format("Cluster %s - population %d", Arrays.toString(cluster.getCentre()), cluster.getWeight()));
        }

        log.info("Distribution: " + Arrays.toString(streamClusterer.getDistribution()));

    }

}