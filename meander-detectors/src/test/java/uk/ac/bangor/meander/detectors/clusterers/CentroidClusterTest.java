package uk.ac.bangor.meander.detectors.clusterers;

import lombok.extern.java.Log;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Will Faithfull
 */
@Log
public class CentroidClusterTest {

    @Test
    public void testDrop() {
        CentroidCluster cluster = new CentroidCluster(1);

        cluster.add(new double[]{ 1 });
        cluster.add(new double[]{ 2 });
        cluster.add(new double[]{ 3 });
        cluster.add(new double[]{ 4 });
        cluster.add(new double[]{ 5 });

        Assert.assertEquals(3.0, cluster.getCentre()[0], 0.0000001);

        cluster.drop(new double[]{ 1 });

        Assert.assertEquals(3.5, cluster.getCentre()[0], 0.0000001);
    }

    @Test
    public void testCov() {
        CentroidCluster cluster = new CentroidCluster(2);

        int n = 30;

        for(int i=0;i<n;i++) {
            cluster.add(new double[]{ i+1, n-i });
        }
        for(double[] row : cluster.getCovariance()) {
            log.info(Arrays.toString(row));
        }
    }

}
