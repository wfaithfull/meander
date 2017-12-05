package uk.ac.bangor.meander.streams;

import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Will Faithfull
 * Class sampler implementation that samples all the data from a .arff file and categorises
 * it into classes for streaming reconstruction.
 */
class ArffClassSampler extends AbstractClassSampler implements ClassSampler {

    private final Map<Integer,Integer> observedClasses = new HashMap<>();
    private final double[] classFrequencies;
    private final List<List<Instance>> instancesByClass = new ArrayList<>();
    private final Random RNG = new Random(System.currentTimeMillis());

    /**
     * Sample the classes from a buffered .arff file.
     * @param file BufferedReader pointing at the file
     * @throws IOException if the file cannot be read.
     */
    ArffClassSampler(BufferedReader file) throws IOException {
        Instances instances = new Instances(file);
        instances.setClassIndex(instances.numAttributes() - 1);

        for(Instance instance: instances) {
            if(!observedClasses.containsKey((int)instance.classValue())) {
                List<Instance> classInstances = new ArrayList<>();
                classInstances.add(instance);
                instancesByClass.add(classInstances);
                observedClasses.put((int)instance.classValue(), instancesByClass.size()-1);
            } else {
                instancesByClass.get(observedClasses.get((int)instance.classValue())).add(instance);
            }
        }

        classFrequencies = new double[instances.numClasses()];

        int totalInstances = instances.numInstances();

        for(int i=0;i<instancesByClass.size();i++) {
            List<Instance> classInstances = instancesByClass.get(i);
            classFrequencies[i] = ((double)classInstances.size()) / totalInstances;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double[] sample(int label) {
        List<Instance> choices = instancesByClass.get(label);
        int choice = RNG.nextInt(choices.size());
        Instance chosen = choices.get(choice);

        // -1 because the last feature is always the class
        Double[] example = new Double[chosen.numValues()-1];
        for(int i=0;i<chosen.numValues()-1;i++) {
            example[i] = chosen.value(i);
        }

        return example;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getClasses() {
        return instancesByClass.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getDistribution() {
        return classFrequencies;
    }
}
