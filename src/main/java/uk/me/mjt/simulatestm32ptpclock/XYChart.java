
package uk.me.mjt.simulatestm32ptpclock;

import java.awt.Dimension;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class XYChart {

    public static void chartTimeStepResults(List<TimeStepResult> toPlot) {
        plotControlOutputGraph(tsr2xydata(toPlot));
    }
    
    public static XYDataset tsr2xydata(List<TimeStepResult> toPlot) {
        XYSeriesCollection ds = new XYSeriesCollection();
        
        XYSeries errorSeries = new XYSeries("Controlled clock error, microseconds");
        toPlot.stream().forEach((ts) -> {
            errorSeries.add(ts.getTrueTimeSeconds(), ts.getClockErrorMicroseconds());
        });
        ds.addSeries(errorSeries);
        
        /*XYSeries outputSeries = new XYSeries("Clock control output, PPB");
        for (TimeStepResult ts : toPlot) {
            outputSeries.add(ts.getTrueTimeSeconds(), ts.controlOutput.intValueExact());
        }
        ds.addSeries(outputSeries);*/
        
        return ds;
    }
    
    public static void plotControlOutputGraph(XYDataset ds) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Control simulation results",
                "Time (s)",
                "Value, see key",
                ds,
                PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setPreferredSize(new Dimension(1600, 1200));
        
        ApplicationFrame af = new ApplicationFrame("Control simulation results");
        af.setContentPane(chartPanel);
        af.pack();
        UIUtils.centerFrameOnScreen(af);
        af.setVisible(true);
    }
    
    

    /*
    public XYChart(String title) {
        super(title);
        XYDataset dataset = createDataset();
        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private static XYDataset createDataset() {
        XYSeriesCollection ds = new XYSeriesCollection();
        
        XYSeries series = new XYSeries("foo");
        series.add(1, 1);
        series.add(2, 2);
        series.add(3, 2);
        series.add(4, 3);
        
        ds.addSeries(series);
        return ds;
    }

    private static JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "XY Line Chart Example",
                "X-Axis",
                "Y-Axis",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        return chart;
    }

    public static void main(String[] args) {
        XYChart demo = new XYChart("JFreeChart: BarChartDemo1.java");
        demo.pack();
        UIUtils.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }*/

}