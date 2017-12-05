package uk.ac.bangor.meander.streams;

import uk.ac.bangor.meander.transitions.Transition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Will Faithfull
 */
public class ChangeStreamBuilder {

    private ClassSampler classSampler;
    private List<DataSource> classDataSources;
    private List<Long> sequenceIndices;
    private List<Transition> transitions;
    private DataSource sequenceDataSource;

    private ChangeStreamBuilder(ClassSampler classSampler) {
        this.classSampler = classSampler;
        this.classDataSources = new ArrayList<>();
        this.sequenceIndices = new ArrayList<>();
        this.transitions = new ArrayList<>();
    }

    public static ChangeStreamBuilder fromArff(Reader reader) throws IOException {
        return new ChangeStreamBuilder(new ArffClassSampler(new BufferedReader(reader)));
    }

    public SequenceBuilder withUniformClassMixture() {
        List<DataSource> mixedSources = new ArrayList<>();
        double[] distribution = new double[classSampler.getClasses()];

        double probability = 1d / classSampler.getClasses();

        for(int i = 0; i < classSampler.getClasses(); i++) {
            mixedSources.add(classSampler.toDataSource(i));
            distribution[i] = probability;
        }

        DataSource dataSource = MixtureDataSource.ofClasses(mixedSources, context -> distribution);
        return new SequenceBuilder(dataSource);
    }

    public SequenceBuilder withClassMixture(Function<Integer, double[]> distributionFunction) {
        List<DataSource> mixedSources = new ArrayList<>();
        for(int i = 0; i < classSampler.getClasses(); i++) {
            mixedSources.add(classSampler.toDataSource(i));
        }
        DataSource dataSource = MixtureDataSource.ofClasses(mixedSources, context -> distributionFunction.apply(mixedSources.size()));
        return new SequenceBuilder(dataSource);
    }

    public SequenceBuilder withClassMixture(double... distribution) {
        List<DataSource> mixedSources = new ArrayList<>();
        for(int i = 0; i < classSampler.getClasses(); i++) {
            mixedSources.add(classSampler.toDataSource(i));
        }
        DataSource dataSource = MixtureDataSource.ofClasses(mixedSources, context -> distribution);
        return new SequenceBuilder(dataSource);
    }

    public class SequenceBuilder {
        DataSource dataSource;

        private SequenceBuilder(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public ChangeStreamBuilder fromStart() {
            if(!ChangeStreamBuilder.this.classDataSources.isEmpty()) {
                throw new IllegalStateException("There is already a starting data source configured.");
            }
            ChangeStreamBuilder.this.classDataSources.add(dataSource);
            return ChangeStreamBuilder.this;
        }

        public ChangeStreamBuilder transition(Transition transition) {
            if(ChangeStreamBuilder.this.classDataSources.isEmpty()) {
                throw new IllegalStateException("There must be a starting data source configured to transition between.");
            }
            ChangeStreamBuilder.this.classDataSources.add(dataSource);
            ChangeStreamBuilder.this.transitions.add(transition);
            return ChangeStreamBuilder.this;
        }
    }

    public Stream<Example> build() {
        sequenceDataSource = MixtureDataSource.ofSequences(this.classDataSources, new SequentialMixtureProvider(transitions));
        return StreamSupport.stream(new DataSourceSpliterator(sequenceDataSource), false);
    }

}
