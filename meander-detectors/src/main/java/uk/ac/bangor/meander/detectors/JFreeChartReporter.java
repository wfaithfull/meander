package uk.ac.bangor.meander.detectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.bangor.meander.streams.StreamContext;

import javax.swing.*;
import java.awt.*;

/**
 * @author Will Faithfull
 */
public class JFreeChartReporter extends JFrame implements ChartReporter {

    XYSeries statistic;
    XYSeries ucl;
    XYSeries lcl;

    public JFreeChartReporter(String title) {
        statistic = new XYSeries("Statistic");
        ucl = new XYSeries("UCL");
        lcl = new XYSeries("LCL");

        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(statistic);
        collection.addSeries(ucl);
        collection.addSeries(lcl);

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
    public void statistic(double statistic, Pipe pipe, StreamContext context) {
        this.statistic.add(context.getIndex(), statistic);
    }

    @Override
    public void ucl(double ucl, Pipe pipe, StreamContext context) {
        this.ucl.add(context.getIndex(), ucl);
    }

    @Override
    public void lcl(double lcl, Pipe pipe, StreamContext context) {
        this.lcl.add(context.getIndex(), lcl);
    }
}
