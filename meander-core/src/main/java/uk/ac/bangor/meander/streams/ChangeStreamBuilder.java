package uk.ac.bangor.meander.streams;

import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Will Faithfull
 */
public class ChangeStreamBuilder {


    private ClassSampler classSampler;

    private ChangeStreamBuilder(ClassSampler classSampler) {
        this.classSampler = classSampler;
    }

    public static ChangeStreamBuilder fromArff(Reader reader) throws IOException {
        return new ChangeStreamBuilder(new ArffClassSampler(new BufferedReader(reader)));
    }

    static class ArffClassSampler extends AbstractClassSampler implements ClassSampler {

        final Map<Integer,Integer> observedClasses = new HashMap<>();
        final List<List<Instance>> instancesByClass = new ArrayList<>();
        final Random RNG = new Random(System.currentTimeMillis());

        public ArffClassSampler(BufferedReader file) throws IOException {
            Instances instances = new Instances(file);
            instances.setClassIndex(instances.numAttributes() - 1);

            for(Instance instance: instances) {
                if(!observedClasses.containsKey(instance.classValue())) {
                    instancesByClass.add(Arrays.asList(instance));
                    observedClasses.put((int)instance.classValue(), instancesByClass.size());
                } else {
                    instancesByClass.get(observedClasses.get((int)instance.classValue())).add(instance);
                }
            }
        }

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

        @Override
        public int getClasses() {
            return instancesByClass.size();
        }
    }

    public Stream<Double[]> withUniformMixture() {
        List<Pair<Double, DataSource>> mixedSources = new ArrayList<>();

        double probability = 1d / classSampler.getClasses();

        for(int i = 0; i < classSampler.getClasses(); i++) {
            mixedSources.add(Pair.of(probability, classSampler.toDataSource(i)));
        }

        InstantMixtureDataSource instantMixtureDataSource = new InstantMixtureDataSource(mixedSources);

        return StreamSupport.stream(new DataSourceSpliterator(instantMixtureDataSource), false);
    }

}
