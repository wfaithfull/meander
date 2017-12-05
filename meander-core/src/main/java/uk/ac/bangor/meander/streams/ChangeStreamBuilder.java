package uk.ac.bangor.meander.streams;

import uk.ac.bangor.meander.MeanderException;
import uk.ac.bangor.meander.transitions.Transition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Will Faithfull
 *
 * Builder class for generating changing data streams. Primary API entry point.
 */
public class ChangeStreamBuilder {

    private ClassSampler                 classSampler;
    private List<ExampleProviderFactory> classExampleProviderFactories;
    private List<Transition>             transitions;
    private ExampleProviderFactory       sequenceExampleProviderFactory;
    private StreamContext                context;

    private ChangeStreamBuilder(ClassSampler classSampler) {
        this.classSampler = classSampler;
        this.classExampleProviderFactories = new ArrayList<>();
        this.transitions = new ArrayList<>();
        this.context = new StreamContext();
    }

    public static ChangeStreamBuilder fromArff(Reader reader) throws IOException {
        return new ChangeStreamBuilder(new ArffClassSampler(new BufferedReader(reader)));
    }

    public static ChangeStreamBuilder fromArff(String file) throws IOException {
        return fromArff(new InputStreamReader(ChangeStreamBuilder.class.getClassLoader().getResourceAsStream(file)));
    }

    public SequenceBuilder withUniformClassMixture() {
        List<ExampleProviderFactory> mixedSources = new ArrayList<>();
        double[] distribution = new double[classSampler.getClasses()];

        double probability = 1d / classSampler.getClasses();

        for(int i = 0; i < classSampler.getClasses(); i++) {
            mixedSources.add(classSampler.toFactory(i));
            distribution[i] = probability;
        }

        ExampleProviderFactory exampleProviderFactory = MixtureDistribution.ofClasses(mixedSources,
                context -> distribution, context);
        return new SequenceBuilder(exampleProviderFactory);
    }

    public SequenceBuilder withClassMixture(Function<Integer, double[]> distributionFunction) {
        List<ExampleProviderFactory> mixedSources = new ArrayList<>();
        for(int i = 0; i < classSampler.getClasses(); i++) {
            mixedSources.add(classSampler.toFactory(i));
        }
        ExampleProviderFactory exampleProviderFactory = MixtureDistribution.ofClasses(mixedSources,
                context -> distributionFunction.apply(mixedSources.size()), context);
        return new SequenceBuilder(exampleProviderFactory);
    }

    public SequenceBuilder withClassMixture(double... distribution) {
        List<ExampleProviderFactory> mixedSources = new ArrayList<>();
        for(int i = 0; i < classSampler.getClasses(); i++) {
            mixedSources.add(classSampler.toFactory(i));
        }
        ExampleProviderFactory exampleProviderFactory = MixtureDistribution.ofClasses(mixedSources,
                context -> distribution, context);
        return new SequenceBuilder(exampleProviderFactory);
    }

    public class SequenceBuilder {
        ExampleProviderFactory exampleProviderFactory;

        private SequenceBuilder(ExampleProviderFactory exampleProviderFactory) {
            this.exampleProviderFactory = exampleProviderFactory;
        }

        public ChangeStreamBuilder fromStart() {
            if(!ChangeStreamBuilder.this.classExampleProviderFactories.isEmpty()) {
                throw new MeanderException("There is already a starting data source configured.");
            }
            ChangeStreamBuilder.this.classExampleProviderFactories.add(exampleProviderFactory);
            return ChangeStreamBuilder.this;
        }

        public ChangeStreamBuilder transition(Transition transition) {
            if(ChangeStreamBuilder.this.classExampleProviderFactories.isEmpty()) {
                throw new MeanderException("There must be a starting data source configured to transition between.");
            }
            ChangeStreamBuilder.this.classExampleProviderFactories.add(exampleProviderFactory);
            ChangeStreamBuilder.this.transitions.add(transition);
            return ChangeStreamBuilder.this;
        }
    }

    public Stream<Example> build() {
        sequenceExampleProviderFactory = MixtureDistribution.ofSources(this.classExampleProviderFactories,
                new SequentialMixingFunction(transitions), context);
        return StreamSupport.stream(new ExampleSpliterator(sequenceExampleProviderFactory, context), false);
    }

}
