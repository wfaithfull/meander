package uk.ac.bangor.meander.detectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

/**
 * @author Will Faithfull
 */
public class JFreeChartReporter extends JFrame implements ChartReporter {

    XYSeries statistic;
    XYSeries threshold;
    private int n = 0;

    public JFreeChartReporter(String title) {
        statistic = new XYSeries("Statistic");
        threshold = new XYSeries("Threshold");

        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(statistic);
        collection.addSeries(threshold);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Time", "Statistic", collection);

        add(new ChartPanel(chart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(640, 480);
            }
        });
        pack();
        setVisible(true);
    }

    @Override
    public void report(State... states) {
        n++;
        statistic.add(n, states[0].getStatistic().get());
        threshold.add(n, states[0].getThreshold().get());
    }
}
