package uk.ac.bangor.meander.detectors.clusterers;

import java.util.List;

/**
 * @author Will Faithfull
 */
public abstract class AbstractStreamClusterer implements StreamClusterer {

    @Override
    public double[] getDistribution() {
        List<Cluster> clusterList = getClusters();

        double[] distribution = new double[clusterList.size()];

        int total = 0;

        for(int i=0;i<distribution.length;i++) {
            total += clusterList.get(i).getWeight();
        }

        for(int i=0;i<distribution.length;i++) {
            distribution[i] = clusterList.get(i).getWeight() / (double)total;
        }

        return distribution;
    }
}
