package uk.ac.bangor.meander.evaluators;

import lombok.Data;
import uk.ac.bangor.meander.MeanderException;
import uk.ac.bangor.meander.transitions.Transition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Will Faithfull
 *
 * Data class holding the evaluation metrics
 */
@Data
public class Evaluation {

    private int              earlyDetection;
    private long             n;
    private long             missed;
    private List<Long>       detections;
    private List<Long>       ttds;
    private List<Long>       rls;
    private List<Long>        falseAlarms;
    private double           idealARL;
    private List<Transition> transitions;

    double arl;
    double ttd;
    double far;
    double mdr;

    public Evaluation(long n, List<Transition> transitions, List<Long> detections) {
        prepare(n, transitions, detections, 0);
    }

    public Evaluation(long n, List<Transition> transitions, List<Long> detections, int earlyDetection) {
        prepare(n, transitions, detections, earlyDetection);
    }

    public Evaluation(List<Evaluation> evaluations) {
        long n = 0;
        List<Long> detections = new ArrayList<>();
        List<Long> ttds = new ArrayList<>();
        List<Long> rls = new ArrayList<>();
        List<Long> falseAlarms = new ArrayList<>();
        List<Transition> transitions = new ArrayList<>();

        int earlyDetection = evaluations.get(0).getEarlyDetection();

        for(Evaluation evaluation : evaluations) {
            if(earlyDetection != evaluation.getEarlyDetection()) {
                throw new MeanderException("Inconsistent values for early detection in evaluations list. Cannot proceed.");
            }
            n += evaluation.getN();
            detections.addAll(evaluation.getDetections());
            ttds.addAll(evaluation.getTtds());
            rls.addAll(evaluation.getRls());
            falseAlarms.addAll(evaluation.getFalseAlarms());
            transitions.addAll(evaluation.getTransitions());
        }

        prepare(n, transitions, detections, earlyDetection);
    }
    
    private void prepare(long n, List<Transition> transitions, List<Long> detections, int earlyDetection) {
        this.n = n;
        this.transitions = transitions;
        this.detections = detections;
        this.ttds = new ArrayList<>();
        this.rls = new ArrayList<>();
        this.falseAlarms = new ArrayList<>();
        this.earlyDetection = earlyDetection;

        computeDetections();
        computeRunLengths();

        if(!this.ttds.isEmpty()) {
            long total = 0;
            for(Long ttd : this.ttds) {
                total += ttd;
            }
            this.ttd = total / (double)this.ttds.size();
        } else {
            this.ttd = n;
        }

        this.mdr = missed / (double)transitions.size();
        this.far = falseAlarms.size() / (double)n;
    }

    private void computeDetections() {
        if(this.transitions.size() == 0) {
            // No transitions means nothing to miss.
            missed = 0;
        } else if (detections.size() == 0) {
            // No detections means all transitions were missed.
            missed = this.transitions.size();
        } else {

            Iterator<Long> iterator = detections.iterator();
            long firstTn = transitions.get(0).getStart() - earlyDetection;
            long detection;
            do {
                detection = iterator.next();
                falseAlarms.add(detection);

                if(!iterator.hasNext()) {
                    break;
                }
            } while (detection < firstTn);

            // Iterate transitions and find matching detections.
            for(int tIdx = 0; tIdx < this.transitions.size(); tIdx++) {

                Transition a = this.transitions.get(tIdx);
                Transition b = null;
                if(tIdx < this.transitions.size() - 1) {
                    b = this.transitions.get(tIdx + 1);
                }

                // Was transition A detected?
                boolean detected = false;
                long bStart = b != null ? b.getStart() - earlyDetection : Long.MAX_VALUE;

                if(iterator.hasNext()) {
                    do {
                        detection = iterator.next();

                        if (detection > bStart) {

                            if (!detected) {
                                missed++;
                            }

                            break;
                        }

                        if (detection >= a.getStart() - earlyDetection && detection < bStart) {
                            if (detected) {
                                this.falseAlarms.add(detection);
                            } else {
                                detected = true;
                                this.ttds.add(detection - a.getStart());
                            }
                        }
                    } while (iterator.hasNext() && detection < bStart);
                } else {
                    missed++;
                }
            }
        }
    }

    private void computeRunLengths() {

        long start = 0;
        for(Transition tn : transitions) {
            this.idealARL += tn.getStart() - start;
            start = tn.getStart();
        }
        this.idealARL = idealARL / (double)transitions.size();

        long last = 0;
        for(long detection : detections) {
            long rl = detection - last;
            last = detection;
            this.rls.add(rl);
        }

        if(!rls.isEmpty()) {
            long total = 0;
            for(Long rl : rls) {
                total += rl;
            }
            this.arl = total / (double)rls.size();
        } else {
            this.arl = n;
        }
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
