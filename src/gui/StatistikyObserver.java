package gui;

import OSPABA.ISimDelegate;
import OSPABA.SimState;
import OSPABA.Simulation;
import agents.agentambulancii.AgentAmbulancii;
import agents.agentambulancii.ManagerAmbulancii;
import agents.agentlekarov.AgentLekarov;
import agents.agentlekarov.ManagerLekarov;
import agents.agentokolia.AgentOkolia;
import agents.agentokolia.ManagerOkolia;
import agents.agentsestier.AgentSestier;
import agents.agentsestier.ManagerSestier;
import agents.agenturgentu.AgentUrgentu;
import agents.agenturgentu.ManagerUrgentu;
import simulation.MySimulation;
import statistiky.Statistic;
import statistiky.TimeWeightedStatistic;

import javax.swing.*;
import java.awt.*;

public class StatistikyObserver implements ISimDelegate {

    private enum StatFormat {
        SECONDS,
        NUMBER,
        PERCENT
    }

    private final JTextArea statistikyArea;

    public StatistikyObserver(JTextArea statistikyArea) {
        this.statistikyArea = statistikyArea;
    }

    @Override
    public void refresh(Simulation sim) {
        MySimulation s = (MySimulation) sim;

        if (s.isMaxSpeed()) {
            return;
        }

        String text = vytvorStatistikyText(s);
        SwingUtilities.invokeLater(() -> statistikyArea.setText(text));
    }

    @Override
    public void simStateChanged(Simulation sim, SimState state) {
        MySimulation s = (MySimulation) sim;

        if (state == SimState.replicationStopped || state == SimState.stopped) {
            String text = vytvorStatistikyText(s);
            SwingUtilities.invokeLater(() -> statistikyArea.setText(text));
        }
    }

    private String vytvorStatistikyText(MySimulation s) {
        if (s.isMaxSpeed()) {
            return vytvorGlobalneStatistikyText(s);
        }

        return vytvorLokalneStatistikyText(s);
    }

    private String vytvorLokalneStatistikyText(MySimulation s) {
        AgentUrgentu urgent = s.agentUrgentu();
        AgentOkolia okolie = s.agentOkolia();
        AgentAmbulancii ambulancie = s.agentAmbulancii();
        AgentSestier sestry = s.agentSestier();
        AgentLekarov lekari = s.agentLekarov();
        double t = s.currentTime();

        StringBuilder sb = new StringBuilder();
        sb.append("  CAKACIE DOBY\n");
        sb.append(radokStat("  Vstupne vysetrenie - PESO", urgent.getCasVCakarniVstupnePesoStat(), StatFormat.SECONDS));
        sb.append(radokStat("  Vstupne vysetrenie - SANITKA", urgent.getCasVCakarniVstupneSanitkaStat(), StatFormat.SECONDS));

        for (int p = 1; p <= 5; p++) {
            sb.append(radokStat("  Osetrenie - priorita " + p,
                    urgent.getCasVCakarniOsetreniePriorita(p),
                    StatFormat.SECONDS));
        }

        sb.append(radokStat("  Celkovy cas v systeme", okolie.getCelkovyCasVSystemeStat(), StatFormat.SECONDS));

        sb.append("\n  RADY\n");
        sb.append(radokTW("  Vstupne vysetrenie (priem. dlzka)", urgent.getDlzkaRaduVstupne(), t, StatFormat.NUMBER));
        sb.append(radokTW("  Osetrenie (priem. dlzka)", urgent.getDlzkaRaduOsetrenie(), t, StatFormat.NUMBER));

        sb.append("\n  VYTAZENIE ZDROJOV\n");
        sb.append(radokTW("  Lekari", lekari.getVytazenieLekarovStat(), t, StatFormat.PERCENT));
        sb.append(radokTW("  Sestry", sestry.getVytazenieSestryStat(), t, StatFormat.PERCENT));
        sb.append(radokTW("  Ambulancie A", ambulancie.getVytazenieAmbulanciiA(), t, StatFormat.PERCENT));
        sb.append(radokTW("  Ambulancie B", ambulancie.getVytazenieAmbulanciiB(), t, StatFormat.PERCENT));

        return sb.toString();
    }

    private String vytvorGlobalneStatistikyText(MySimulation s) {
        StringBuilder sb = new StringBuilder();
        sb.append("  CAKACIE DOBY\n");
        sb.append(radokStatIS("  Vstupne vysetrenie - PESO", s.getGlobCasVCakarniVstupnePeso(), StatFormat.SECONDS));
        sb.append(radokStatIS("  Vstupne vysetrenie - SANITKA", s.getGlobCasVCakarniVstupneSanitka(), StatFormat.SECONDS));

        for (int p = 1; p <= 5; p++) {
            sb.append(radokStatIS("  Osetrenie - priorita " + p,
                    s.getGlobCasVCakarniOsetreniePriorita(p),
                    StatFormat.SECONDS));
        }

        sb.append(radokStatIS("  Celkovy cas v systeme", s.getGlobCelkovyCasVSysteme(), StatFormat.SECONDS));

        sb.append("\n  RADY\n");
        sb.append(radokStatIS("  Vstupne vysetrenie (priem. dlzka)", s.getGlobDlzkaRaduVstupne(), StatFormat.NUMBER));
        sb.append(radokStatIS("  Osetrenie (priem. dlzka)", s.getGlobDlzkaRaduOsetrenie(), StatFormat.NUMBER));

        sb.append("\n  VYTAZENIE ZDROJOV\n");
        sb.append(radokStatIS("  Lekari", s.getGlobVytazenieLekarov(), StatFormat.PERCENT));
        sb.append(radokStatIS("  Sestry", s.getGlobVytazenieSestier(), StatFormat.PERCENT));
        sb.append(radokStatIS("  Ambulancie A", s.getGlobVytazenieAmbulanciiA(), StatFormat.PERCENT));
        sb.append(radokStatIS("  Ambulancie B", s.getGlobVytazenieAmbulanciiB(), StatFormat.PERCENT));

        return sb.toString();
    }

    private String radokStat(String nazov, Statistic stat, StatFormat fmt) {
        String pad = String.format("%-42s", nazov + ":");
        if (stat == null || stat.getCount() == 0) {
            return pad + " -\n";
        }

        return pad + " " + fmtVal(stat.getAverage(), fmt) + "\n";
    }

    private String radokStatIS(String nazov, Statistic stat, StatFormat fmt) {
        String pad = String.format("%-42s", nazov + ":");
        if (stat == null || stat.getCount() == 0) {
            return pad + " -\n";
        }

        String is = fmtIS(stat, fmt);
        return pad + " " + fmtVal(stat.getAverage(), fmt) + "  " + is + "\n";
    }

    private String radokTW(String nazov, TimeWeightedStatistic stat, double t, StatFormat fmt) {
        String pad = String.format("%-42s", nazov + ":");
        if (stat == null) {
            return pad + " -\n";
        }

        return pad + " " + fmtVal(stat.getAverage(t), fmt) + "\n";
    }

    private String fmtVal(double value, StatFormat fmt) {
        switch (fmt) {
            case SECONDS:
                return formatTime(value);
            case PERCENT:
                return String.format("%8.2f %%", value * 100.0);
            case NUMBER:
            default:
                return String.format("%9.3f", value);
        }
    }

    private String fmtIS(Statistic stat, StatFormat fmt) {
        double[] iv = stat.getConfidenceInterval();
        if (iv == null) {
            return "(IS: -)";
        }

        return "(IS: " + fmtVal(iv[0], fmt).trim()
                + " - " + fmtVal(iv[1], fmt).trim() + ")";
    }

    private String formatTime(double totalSeconds) {
        if (Double.isNaN(totalSeconds) || totalSeconds < 0) {
            return "00:00:00";
        }

        int total = (int) Math.round(totalSeconds);
        return String.format("%02d:%02d:%02d", total / 3600, (total % 3600) / 60, total % 60);
    }
}
