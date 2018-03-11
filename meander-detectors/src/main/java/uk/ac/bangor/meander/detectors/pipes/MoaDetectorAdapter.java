package uk.ac.bangor.meander.detectors.pipes;

import moa.classifiers.core.driftdetection.*;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Will Faithfull
 *
 * Adapts the univariate change detectors from MOA into the meander format.
 */
public class MoaDetectorAdapter implements Pipe<Double, Boolean> {

    private ChangeDetector moaDetector;
    public MoaDetectorAdapter(ChangeDetector moaDetector) {
        this.moaDetector = moaDetector;
        this.moaDetector.prepareForUse();
    }

    public static MoaDetectorAdapter cusum() {
        return new MoaDetectorAdapter(new CusumDM());
    }

    public static MoaDetectorAdapter adwin() {
        return new MoaDetectorAdapter(new ADWINChangeDetector());
    }

    public static MoaDetectorAdapter geometricMovingAverage() {
        return new MoaDetectorAdapter(new GeometricMovingAverageDM());
    }

    public static MoaDetectorAdapter ddm() {
        return new MoaDetectorAdapter(new DDM());
    }

    public static MoaDetectorAdapter eddm() {
        return new MoaDetectorAdapter(new EDDM());
    }

    public static MoaDetectorAdapter ewma() {
        return new MoaDetectorAdapter(new EWMAChartDM());
    }

    public static MoaDetectorAdapter pageHinkley() {
        return new MoaDetectorAdapter(new PageHinkleyDM());
    }

    public static MoaDetectorAdapter hddmA() {
        return new MoaDetectorAdapter(new HDDM_A_Test());
    }

    public static MoaDetectorAdapter hddmW() {
        return new MoaDetectorAdapter(new HDDM_W_Test());
    }

    public static MoaDetectorAdapter seed() {
        return new MoaDetectorAdapter(new SEEDChangeDetector());
    }

    public static MoaDetectorAdapter seq1() {
        return new MoaDetectorAdapter(new SeqDrift1ChangeDetector());
    }

    public static MoaDetectorAdapter seq2() {
        return new MoaDetectorAdapter(new SeqDrift2ChangeDetector());
    }

    public static List<Pipe<Double, Boolean>> allMoaDetectors() {
        ArrayList<Pipe<Double,Boolean>> detectors = new ArrayList<>();
        detectors.add(cusum());
        detectors.add(adwin());
        detectors.add(ddm());
        detectors.add(eddm());
        detectors.add(geometricMovingAverage());
        detectors.add(pageHinkley());
        detectors.add(ewma());
        detectors.add(hddmA());
        detectors.add(hddmW());
        detectors.add(seed());
        detectors.add(seq1());
        detectors.add(seq2());
        return detectors;
    }

    @Override
    public Boolean execute(Double value, StreamContext context) {
        moaDetector.input(value);
        return moaDetector.getChange();
    }
}
