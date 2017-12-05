import org.junit.Assert;
import org.junit.Test;
import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.streams.Example;
import uk.ac.bangor.meander.transitions.AbruptTransition;
import uk.ac.bangor.meander.transitions.LinearTransition;
import uk.ac.bangor.meander.transitions.LogisticTransition;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Will Faithfull
 */
public class ArffStreamTest {

    @Test
    public void testArffStream() throws IOException {
        Stream<Example> arffStream = ChangeStreamBuilder
                .fromArff("abalone.arff")
                .withUniformClassMixture().fromStart()
                .build();

        int n = 100;
        int counter = 0;

        for(Example example : arffStream.limit(n).collect(Collectors.toList())) {
            counter++;
            Assert.assertTrue(example.getContext().getSequence() == 0);
        }

        Assert.assertEquals(n, counter);
    }

    @Test
    public void testAbruptTransition() throws IOException {
        Stream<Example> arffStream = ChangeStreamBuilder
                .fromArff("abalone.arff")
                .withUniformClassMixture().fromStart()
                .withClassMixture(1.0, 0.0, 0.0).transition(new AbruptTransition(50))
                .build();

        int n = 100;
        int counter = 0;

        Iterator<Example> iterator = arffStream.limit(n).iterator();
        while (iterator.hasNext()){
            Example example = iterator.next();

            if(counter < 50) {
                Assert.assertTrue(example.getContext().getSequence() == 0);
            } else {
                Assert.assertTrue(example.getContext().getSequence() == 1);
                Assert.assertTrue(example.getContext().getLabel() == 0);
            }
            counter++;
        }

        Assert.assertEquals(n, counter);
    }

    @Test
    public void testLinearTransition() throws IOException {

        int L = 10;
        int C = 50;
        
        Stream<Example> arffStream = ChangeStreamBuilder
                .fromArff("abalone.arff")
                .withUniformClassMixture().fromStart()
                .withClassMixture(1.0, 0.0, 0.0).transition(new LinearTransition(C, C + L))
                .build();

        int n = 100;
        int counter = 0;

        Iterator<Example> iterator = arffStream.limit(n).iterator();
        while (iterator.hasNext()){
            Example example = iterator.next();

            if(counter < C) {
                Assert.assertTrue(example.getContext().getSequence() == 0);
            } else if (counter > C+L) {
                Assert.assertTrue(example.getContext().getSequence() == 1);
                Assert.assertTrue(example.getContext().getLabel() == 0);
            }
            counter++;
        }

        Assert.assertEquals(n, counter);
    }

    @Test
    public void testLogisticTransition() throws IOException {

        int L = 10;
        int C = 50;

        Stream<Example> arffStream = ChangeStreamBuilder
                .fromArff("abalone.arff")
                .withUniformClassMixture().fromStart()
                .withClassMixture(1.0, 0.0, 0.0).transition(new LogisticTransition(C, C + L))
                .build();

        int n = 100;
        int counter = 0;

        Iterator<Example> iterator = arffStream.limit(n).iterator();
        while (iterator.hasNext()){
            Example example = iterator.next();

            if(counter < C) {
                Assert.assertTrue(example.getContext().getSequence() == 0);
            } else if (counter > C+L) {
                Assert.assertTrue(example.getContext().getSequence() == 1);
                Assert.assertTrue(example.getContext().getLabel() == 0);
            }
            counter++;
        }

        Assert.assertEquals(n, counter);
    }

    @Test
    public void testMultipleTransitions() throws IOException {

        int L = 10;
        int C = 50;

        Stream<Example> arffStream = ChangeStreamBuilder
                .fromArff("abalone.arff")
                .withUniformClassMixture().fromStart()
                .withClassMixture(1.0, 0.0, 0.0).transition(new LogisticTransition(C, C + L))
                .withClassMixture(0.0, 1.0, 0.0).transition(new LinearTransition(C*2, (C*2)+L))
                .withClassMixture(0.0, 0.0, 1.0).transition(new AbruptTransition(C*3))
                .build();

        int n = 300;
        int counter = 0;

        Iterator<Example> iterator = arffStream.limit(n).iterator();
        while (iterator.hasNext()){
            Example example = iterator.next();

            if(counter < C) {
                Assert.assertTrue(example.getContext().getSequence() == 0);
            } else if (counter > C+L && counter < C*2) {
                Assert.assertTrue(example.getContext().getSequence() == 1);
                Assert.assertTrue(example.getContext().getLabel() == 0);
            } else if (counter > C*2+L && counter < C*3) {
                Assert.assertTrue(example.getContext().getSequence() == 2);
                Assert.assertTrue(example.getContext().getLabel() == 1);
            } else if (counter > C*3) {
                Assert.assertTrue(example.getContext().getSequence() == 3);
                Assert.assertTrue(example.getContext().getLabel() == 2);
            }
            counter++;
        }

        Assert.assertEquals(n, counter);
    }

}
