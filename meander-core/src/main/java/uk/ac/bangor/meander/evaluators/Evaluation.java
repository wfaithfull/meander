package uk.ac.bangor.meander.evaluators;

import lombok.Data;
import uk.ac.bangor.meander.transitions.Transition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Will Faithfull
 *
 * Data class holding the evaluation metrics
 */
@Data
public class Evaluation {

    private long n;
    private long missed;
    private List<Long> detections;
    private List<Long> ttds;
    private List<Long> rls;
    private List<Long> falseAlarms;
    private double idealARL;
    private List<Transition> transitions;

    double arl;
    double ttd;
    double far;
    double mdr;

    public Evaluation(long n, List<Transition> transitions, List<Long> detections, List<Long> ttds,
                      List<Long> rls, List<Long> falseAlarms, double idealARL) {
        prepare(n, transitions, detections, ttds, rls, falseAlarms, idealARL);
    }

    public Evaluation(List<Evaluation> evaluations) {
        long n = 0;
        long missed = 0;
        List<Long> detections = new ArrayList<>();
        List<Long> ttds = new ArrayList<>();
        List<Long> rls = new ArrayList<>();
        List<Long> falseAlarms = new ArrayList<>();
        double idealARL = 0;
        List<Transition> transitions = new ArrayList<>();

        for(Evaluation evaluation : evaluations) {
            n += evaluation.getN();
            missed += evaluation.getMissed();
            detections.addAll(evaluation.getDetections());
            ttds.addAll(evaluation.getTtds());
            rls.addAll(evaluation.getRls());
            falseAlarms.addAll(evaluation.getFalseAlarms());
            idealARL += evaluation.idealARL;
            transitions.addAll(evaluation.getTransitions());
        }

        prepare(n, transitions, detections, ttds, rls, falseAlarms, idealARL/evaluations.size());
    }
    
    private void prepare(long n, List<Transition> transitions, List<Long> detections, List<Long> ttds,
                         List<Long> rls, List<Long> falseAlarms, double idealARL) {
        this.n = n;
        this.transitions = transitions;
        this.detections = detections;
        this.ttds = ttds;
        this.rls = rls;
        this.falseAlarms = falseAlarms;
        this.idealARL = idealARL;

        if(!rls.isEmpty()) {
            long total = 0;
            for(Long rl : rls) {
                total += rl;
            }
            this.arl = total / (double)rls.size();
        } else {
            this.arl = n;
        }

        if(!ttds.isEmpty()) {
            long total = 0;
            for(Long ttd : ttds) {
                total += ttd;
            }
            this.ttd = total / (double)ttds.size();
        } else {
            this.ttd = n;
        }

        if(!transitions.isEmpty()) {
            missed = transitions.size() - ttds.size();
        }

        this.mdr = missed / n;
        this.far = falseAlarms.size() / (double)n;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append(" [\n");
        builder.append(String.format("\tn=%d\n", getN()));
        builder.append(String.format("\tARL=%.10f, Ideal=%.10f\n", getArl(), getIdealARL()));
        builder.append(String.format("\tTTD=%.10f\n", getTtd()));
        builder.append(String.format("\tFAR=%.10f, %d false alarms\n", getFar(), falseAlarms.size()));
        builder.append(String.format("\tMDR=%.10f, Missed %d/%d\n]", getMdr(), getMissed(),
                getTransitions().size()));

        return builder.toString();
    }
}
