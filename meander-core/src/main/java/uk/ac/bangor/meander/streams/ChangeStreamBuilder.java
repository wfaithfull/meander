package uk.ac.bangor.meander.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Will Faithfull
 */
public class ChangeStreamBuilder {


    private ClassSampler classSampler;
    private List<DataSource> classDataSources;
    private List<Long> sequenceIndices;
    private DataSource sequenceDataSource;

    private ChangeStreamBuilder(ClassSampler classSampler) {
        this.classSampler = classSampler;
        this.classDataSources = new ArrayList<>();
        this.sequenceIndices = new ArrayList<>();
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

    public SequenceBuilder withClassMixture(double... distribution) {
        List<DataSource> mixedSources = new ArrayList<>();
        for(int i = 0; i < classSampler.getClasses(); i++) {
            mixedSources.add(classSampler.toDataSource(i));
        }
        DataSource dataSource = MixtureDataSource.ofClasses(mixedSources, context -> distribution);
        return new SequenceBuilder(dataSource);
    }

    public ChangeStreamBuilder withChangePoints(long... changePoints) {
        sequenceDataSource = MixtureDataSource.ofSequences(this.classDataSources, new SequenceMixture(changePoints));
        return this;
    }

    public class SequenceBuilder {
        DataSource dataSource;

        private SequenceBuilder(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public ChangeStreamBuilder fromStart() {
            return at(0);
        }

        public ChangeStreamBuilder at(long start) {
            ChangeStreamBuilder.this.classDataSources.add(dataSource);
            List<Long> sequenceIndices = ChangeStreamBuilder.this.sequenceIndices;

            if(start == 0) {
                return ChangeStreamBuilder.this;
            }

            if(sequenceIndices.size() > 0) {
                long lastSequenceStart = sequenceIndices.get(sequenceIndices.size()-1);

                if(start <= lastSequenceStart)
                    throw new IllegalArgumentException("Sequence must start after previous sequence (" + lastSequenceStart + ")");
            }

            ChangeStreamBuilder.this.sequenceIndices.add(start);
            return ChangeStreamBuilder.this;
        }
    }

    private static class SequenceMixture implements MixtureProvider {

        private double[] distribution;
        private Stack<Long> startIndices;
        private int index;

        SequenceMixture(long... startIndices) {
            prepare(startIndices);
        }

        private void prepare(long[] indices) {

            if(indices.length < 2)
                throw new IllegalArgumentException("Need at least two sources to mix");

            this.startIndices = new Stack<>();

            for(int i=indices.length-1; i>=0; i--) {
                this.startIndices.push(indices[i]);
            }

            distribution = new double[indices.length+1];
            distribution[0] = 1.0;
        }

        SequenceMixture(List<Long> sequenceIndices) {
            long[] startIndices = new long[sequenceIndices.size()];
            for(int i=0; i < sequenceIndices.size(); i++)
                startIndices[i] = sequenceIndices.get(i);

            prepare(startIndices);
        }

        @Override
        public double[] getDistribution(StreamContext context) {

            if(!startIndices.empty() && context.getIndex() > startIndices.peek()) {
                startIndices.pop();
                index++;

                distribution[index-1] = 0;
                distribution[index] = 1.0;
            }

            return distribution;
        }
    }
    public Stream<Example> build() {
        if(sequenceDataSource == null) {
            sequenceDataSource = MixtureDataSource.ofSequences(this.classDataSources, new SequenceMixture(sequenceIndices));
        }
        return StreamSupport.stream(new DataSourceSpliterator(sequenceDataSource), false);
    }

}
