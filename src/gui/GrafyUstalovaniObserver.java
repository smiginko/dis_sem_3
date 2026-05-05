package gui;

import OSPABA.ISimDelegate;
import OSPABA.SimState;
import OSPABA.Simulation;
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

public class GrafyUstalovaniObserver implements ISimDelegate {

    private final XYSeries seriePeso = new XYSeries("PESO");
    private final XYSeries serieSanitka = new XYSeries("SANITKA");
    private final ChartPanel pesoPanel;
    private final ChartPanel sanitkaPanel;

    private int repCount = 0;
    private int pesoRepCount = 0;
    private int sanitkaRepCount = 0;
    private double runAvgPeso = 0;
    private double runAvgSanitka = 0;

    public GrafyUstalovaniObserver() {
        pesoPanel = new ChartPanel(vytvorGraf(
                "Ustálenie –  cas od vstupu po zaciatok osetrenia (PESO)",
                seriePeso, new Color(0, 100, 200)));
        sanitkaPanel = new ChartPanel(vytvorGraf(
                "Ustálenie – cas od vstupu po zaciatok osetrenia (SANITKA)",
                serieSanitka, new Color(180, 40, 0)));
    }

    public ChartPanel getPesoPanel() { return pesoPanel; }
    public ChartPanel getSanitkaPanel() { return sanitkaPanel; }

    public void reset() {
        repCount = pesoRepCount = sanitkaRepCount = 0;
        runAvgPeso = runAvgSanitka = 0;
        SwingUtilities.invokeLater(() -> {
            seriePeso.clear();
            serieSanitka.clear();
        });
    }

    @Override
    public void refresh(Simulation sim) {}

    @Override
    public void simStateChanged(Simulation simulation, SimState state) {
        if (state != SimState.replicationStopped) return;

        MySimulation s = (MySimulation) simulation;
        int rep = s.currentReplication();

        double pesoVal = Double.NaN;
        double sanitkaVal = Double.NaN;

        if (s.getGlobCasOdVstupuPoZaciatokOsetreniaPeso().getCount() > 0) {
            pesoVal = s.getGlobCasOdVstupuPoZaciatokOsetreniaPeso().getAverage();
        }

        if (s.getGlobCasOdVstupuPoZaciatokOsetreniaSanitka().getCount() > 0) {
            sanitkaVal = s.getGlobCasOdVstupuPoZaciatokOsetreniaSanitka().getAverage();
        }

        final double fp = pesoVal;
        final double fs = sanitkaVal;

        SwingUtilities.invokeLater(() -> {
            if (!Double.isNaN(fp)) seriePeso.add(rep, fp);
            if (!Double.isNaN(fs)) serieSanitka.add(rep, fs);
        });
    }

    private JFreeChart vytvorGraf(String title, XYSeries series, Color color) {
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Replikácia", "Priem. čakanie (s)",
                dataset, PlotOrientation.VERTICAL, false, true, false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(true);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, color);
        renderer.setSeriesStroke(0, new BasicStroke(1.5f));
        plot.setRenderer(renderer);

        return chart;
    }
}
