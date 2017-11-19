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

        final Map<Integer, List<Instance>> instancesByClass = new HashMap<>();
        final Random RNG = new Random(System.currentTimeMillis());
        final int[] classes;

        public ArffClassSampler(BufferedReader file) throws IOException {
            Instances instances = new Instances(file);
            instances.setClassIndex(instances.numAttributes() - 1);

            for(Instance instance: instances) {
                if(!instancesByClass.containsKey(instance.classValue())) {
                    instancesByClass.put((int) instance.classValue(), Arrays.asList(instance));
                } else {
                    instancesByClass.get((int) instance.classValue()).add(instance);
                }
            }

            classes = new int[instancesByClass.size()];
            Iterator<Integer> classesIterator = instancesByClass.keySet().iterator();
            for(int i=0;i<instancesByClass.size();i++) {
                classes[i] = classesIterator.next();
            }
        }

        @Override
        public Double[] sample(int source) {
            List<Instance> choices = instancesByClass.get(source);
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
        public int[] getClasses() {
            return classes;
        }
    }

    public Stream<Double[]> withUniformMixture() {
        List<Pair<Double, DataSource>> mixedSources = new ArrayList<>();

        double probability = 1d / classSampler.getClasses().length;

        for(int i = 0; i < classSampler.getClasses().length; i++) {
            mixedSources.add(Pair.of(probability, classSampler.toDataSource(i)));
        }

        InstantMixtureDataSource instantMixtureDataSource = new InstantMixtureDataSource(mixedSources);

        return StreamSupport.stream(new DataSourceSpliterator(instantMixtureDataSource), false);
    }

}
