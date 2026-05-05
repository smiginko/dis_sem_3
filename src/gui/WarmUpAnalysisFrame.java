package gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import simulation.MySimulation;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class WarmUpAnalysisFrame extends JFrame {

    private volatile MySimulation analysisSim = null;

    private JTextField durField;
    private JTextField repsField;
    private JButton startBtn;
    private JButton stopBtn;
    private XYSeriesCollection dataset;
    private JFreeChart chart;

    public WarmUpAnalysisFrame() {
        setTitle("Analýza zahrievania (Warm-up)");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        initGui();
    }

    private void initGui() {
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        durField  = new JTextField("2419200", 10);
        repsField = new JTextField("10", 4);

        startBtn = new JButton("Spustiť analýzu");
        stopBtn  = new JButton("Stop");
        stopBtn.setEnabled(false);

        topBar.add(new JLabel("Trvanie replikácie (s):"));
        topBar.add(durField);
        topBar.add(new JLabel("Replikácie:"));
        topBar.add(repsField);
        topBar.add(startBtn);
        topBar.add(stopBtn);
        add(topBar, BorderLayout.NORTH);

        dataset = new XYSeriesCollection();
        chart = ChartFactory.createXYLineChart(
                "Počet pacientov v systéme",
                "Čas (s)",
                "Priemerný počet pacientov",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(true);
        plot.setRenderer(new XYLineAndShapeRenderer(true, false));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        add(new ChartPanel(chart), BorderLayout.CENTER);

        startBtn.addActionListener(e -> runAnalysis());
        stopBtn.addActionListener(e -> {
            if (analysisSim != null) analysisSim.stopSimulation();
            stopBtn.setEnabled(false);
            startBtn.setEnabled(true);
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (analysisSim != null) analysisSim.stopSimulation();
            }
        });
    }

    private void runAnalysis() {
        dataset.removeAllSeries();
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);

        double duration = Double.parseDouble(durField.getText());
        int reps = Integer.parseInt(repsField.getText());

        new Thread(() -> {
            List<List<Double>> allYData = new ArrayList<>();
            List<Double> commonX = null;

            for (int r = 0; r < reps; r++) {
                analysisSim = new MySimulation();
                analysisSim.setCollectAnalysisData(true);
                analysisSim.setTurboDesired();
                analysisSim.simulate(1, duration);

                List<Double> xData = new ArrayList<>(analysisSim.getAnalysisTimes());
                List<Double> yData = new ArrayList<>(analysisSim.getAnalysisValues());
                int repNum = r + 1;

                if (xData.isEmpty()) {
                    System.out.println("Varovanie: Replikácia " + repNum + " nevyprodukovala žiadne dáta.");
                    continue;
                }

                if (commonX == null) commonX = xData;
                allYData.add(yData);

                XYSeries series = buildSeries("Replikácia " + repNum, xData, yData);

                try {
                    SwingUtilities.invokeAndWait(() -> {
                        dataset.addSeries(series);
                        XYPlot plot = chart.getXYPlot();
                        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
                        int idx = dataset.getSeriesCount() - 1;
                        renderer.setSeriesStroke(idx, new BasicStroke(1.0f));
                        renderer.setSeriesPaint(idx, new Color(180, 180, 180, 60));
                        chart.fireChartChanged();
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex.getCause());
                }
            }

            if (!allYData.isEmpty() && commonX != null) {
                List<Double> avgY = new ArrayList<>();
                int maxLen = commonX.size();
                for (int i = 0; i < maxLen; i++) {
                    double sum = 0; int count = 0;
                    for (List<Double> repData : allYData) {
                        if (i < repData.size()) { sum += repData.get(i); count++; }
                    }
                    avgY.add(count > 0 ? sum / count : 0.0);
                }
                XYSeries avgSeries = buildSeries("CELKOVÝ PRIEMER", commonX, avgY);
                SwingUtilities.invokeLater(() -> {
                    dataset.addSeries(avgSeries);
                    XYPlot plot = chart.getXYPlot();
                    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
                    int idx = dataset.getSeriesCount() - 1;
                    renderer.setSeriesPaint(idx, Color.RED);
                    renderer.setSeriesStroke(idx, new BasicStroke(4.0f));
                    chart.fireChartChanged();
                });
            }

            SwingUtilities.invokeLater(() -> {
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                revalidate();
                repaint();
            });
        }).start();
    }

    private XYSeries buildSeries(String name, List<Double> xData, List<? extends Number> yData) {
        XYSeries series = new XYSeries(name);

        for (int i = 0; i < xData.size(); i++) {
            if (i % 20 == 0) {
                series.add(xData.get(i).doubleValue(), yData.get(i).doubleValue());
            }
        }
        return series;
    }
}
