package gui;

import OSPABA.ISimDelegate;
import OSPABA.SimState;
import OSPABA.Simulation;
import entity.Ambulancia;
import entity.Lekar;
import entity.Sestra;
import simulation.MySimulation;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ZdrojeObserver implements ISimDelegate {

    private static class ZdrojRow {
        final int id;
        final String stlpec2;
        final String stlpec3;

        ZdrojRow(int id, String stlpec2, String stlpec3) {
            this.id = id;
            this.stlpec2 = stlpec2;
            this.stlpec3 = stlpec3;
        }
    }

    private final JTable ambulancieTable;
    private final JTable sestryTable;
    private final JTable lekariTable;

    public ZdrojeObserver(JTable ambulancieTable, JTable sestryTable, JTable lekariTable) {
        this.ambulancieTable = ambulancieTable;
        this.sestryTable = sestryTable;
        this.lekariTable = lekariTable;

        setupRenderer(ambulancieTable, 2);
        setupRenderer(sestryTable, 1);
        setupRenderer(lekariTable, 1);
    }

    @Override
    public void refresh(Simulation sim) {
        if (sim.isMaxSpeed()) return;

        MySimulation s = (MySimulation) sim;

        List<ZdrojRow> ambulancieSnapshot = vytvorAmbulancieSnapshot(s);
        List<ZdrojRow> sestrySnapshot = vytvorSestrySnapshot(s);
        List<ZdrojRow> lekariSnapshot = vytvorLekariSnapshot(s);

        SwingUtilities.invokeLater(() -> {
            updateTable(ambulancieTable, ambulancieSnapshot);
            updateTable(sestryTable, sestrySnapshot);
            updateTable(lekariTable, lekariSnapshot);
        });
    }

    @Override
    public void simStateChanged(Simulation sim, SimState state) {
        if (state == SimState.replicationRunning) {
            SwingUtilities.invokeLater(() -> {
                clearTable(ambulancieTable);
                clearTable(sestryTable);
                clearTable(lekariTable);
            });
        }
    }

    private void updateTable(JTable table, List<ZdrojRow> rows) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (ZdrojRow row : rows) {
            model.addRow(new Object[]{
                    row.id,
                    row.stlpec2,
                    row.stlpec3
            });
        }
    }

    private void clearTable(JTable table) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
    }

    private void setupRenderer(JTable table, int stavColumn) {
        table.getColumnModel().getColumn(stavColumn).setCellRenderer(new StatusRenderer());
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value == null) return c;
            String status = value.toString();
            c.setFont(c.getFont().deriveFont(Font.BOLD));
            if (!status.startsWith("OBS")) {
                c.setBackground(new Color(200, 255, 200));
                c.setForeground(new Color(0, 100, 0));
            } else {
                c.setBackground(new Color(255, 230, 150));
                c.setForeground(new Color(150, 100, 0));
            }
            return c;
        }
    }

    private List<ZdrojRow> vytvorAmbulancieSnapshot(MySimulation s) {
        List<ZdrojRow> rows = new java.util.ArrayList<>();

        for (Ambulancia a : s.agentAmbulancii().getAmbulancies()) {
            rows.add(new ZdrojRow(
                    a.id(),
                    a.getTyp().toString(),
                    a.isJeObsadena() ? "OBSADENÁ" : "VOĽNÁ"
            ));
        }

        return rows;
    }

    private List<ZdrojRow> vytvorSestrySnapshot(MySimulation s) {
        List<ZdrojRow> rows = new java.util.ArrayList<>();

        for (Sestra sestra : s.agentSestier().getSestry()) {
            String pacientInfo = sestra.getAktualnyPacient() != null
                    ? "Pac. " + sestra.getAktualnyPacient().id()
                    : "-";

            rows.add(new ZdrojRow(
                    sestra.id(),
                    sestra.jeObsadena() ? "OBSADENÁ" : "VOĽNÁ",
                    pacientInfo
            ));
        }

        return rows;
    }

    private List<ZdrojRow> vytvorLekariSnapshot(MySimulation s) {
        List<ZdrojRow> rows = new java.util.ArrayList<>();

        for (Lekar lekar : s.agentLekarov().getLekari()) {
            String pacientInfo = lekar.getAktualnyPacient() != null
                    ? "Pac. " + lekar.getAktualnyPacient().id()
                    : "-";

            rows.add(new ZdrojRow(
                    lekar.id(),
                    lekar.jeObsadeny() ? "OBSADENÝ" : "VOĽNÝ",
                    pacientInfo
            ));
        }

        return rows;
    }
}
