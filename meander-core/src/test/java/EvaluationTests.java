import org.junit.Assert;
import org.junit.Test;
import uk.ac.bangor.meander.evaluators.Evaluation;
import uk.ac.bangor.meander.transitions.AbruptTransition;
import uk.ac.bangor.meander.transitions.Transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Will Faithfull
 */
public class EvaluationTests {

    @Test
    public void testEvaluation() {

        long N = 1000;
        List<Transition> transitionList = Arrays.asList(
                new AbruptTransition(250),
                new AbruptTransition(500),
                new AbruptTransition(750)
        );

        List<Long> detectionList = Arrays.asList(255L, 505L, 755L);

        Evaluation evaluation = new Evaluation(N, transitionList, detectionList);

        Assert.assertEquals(5, evaluation.getTtd(), 0.000001);
        Assert.assertEquals(0, evaluation.getMdr(), 0.000001);
        Assert.assertEquals(arl(detectionList), evaluation.getArl(), 0.000001);
        Assert.assertEquals(arl(toStarts(transitionList)), evaluation.getIdealARL(), 0.000001);
        Assert.assertEquals(0, evaluation.getFalseAlarms().size());
    }

    @Test
    public void testFalsePositiveAlreadyDetected() {

        long N = 1000;
        List<Transition> transitionList = Arrays.asList(
                new AbruptTransition(250),
                new AbruptTransition(500),
                new AbruptTransition(750)
        );

        List<Long> detectionList = Arrays.asList(255L, 505L, 755L, 765L);

        Evaluation evaluation = new Evaluation(N, transitionList, detectionList);

        Assert.assertEquals(5, evaluation.getTtd(), 0.000001);
        Assert.assertEquals(0, evaluation.getMdr(), 0.000001);
        Assert.assertEquals(arl(detectionList), evaluation.getArl(), 0.000001);
        Assert.assertEquals(1, evaluation.getFalseAlarms().size());
    }

    @Test
    public void testFalsePositiveBeforeFirst() {

        long N = 1000;
        List<Transition> transitionList = Arrays.asList(
                new AbruptTransition(250),
                new AbruptTransition(500),
                new AbruptTransition(750)
        );

        List<Long> detectionList = Arrays.asList(210L, 255L, 505L, 755L);

        Evaluation evaluation = new Evaluation(N, transitionList, detectionList);

        Assert.assertEquals(5, evaluation.getTtd(), 0.000001);
        Assert.assertEquals(0, evaluation.getMdr(), 0.000001);
        Assert.assertEquals(arl(detectionList), evaluation.getArl(), 0.000001);
        Assert.assertEquals(1, evaluation.getFalseAlarms().size());
    }

    @Test
    public void testHalfMissed() {

        long N = 1000;
        List<Transition> transitionList = Arrays.asList(
                new AbruptTransition(200),
                new AbruptTransition(400),
                new AbruptTransition(600),
                new AbruptTransition(800)
        );

        List<Long> detectionList = Arrays.asList(210L, 620L);

        Evaluation evaluation = new Evaluation(N, transitionList, detectionList);

        Assert.assertEquals(detectionList.size(), evaluation.getTtds().size());
        Assert.assertEquals(15, evaluation.getTtd(), 0.000001);
        Assert.assertEquals(.5, evaluation.getMdr(), 0.000001);
        Assert.assertEquals(arl(detectionList), evaluation.getArl(), 0.000001);
        Assert.assertEquals(0, evaluation.getFalseAlarms().size());
    }

    static double arl(List<Long> events) {

        final List<Long> atZero = new ArrayList<>();
        atZero.add(0L);
        atZero.addAll(events);

        return IntStream.range(1, atZero.size())
                .mapToLong(idx -> atZero.get(idx) - atZero.get(idx-1))
                .average().getAsDouble();
    }

    static List<Long> toStarts(List<Transition> transitions) {
        return transitions.stream().map(tn -> tn.getStart()).collect(Collectors.toList());
    }

}
