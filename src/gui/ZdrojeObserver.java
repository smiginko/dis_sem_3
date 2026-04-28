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

        List<Ambulancia> ambulancie = s.agentAmbulancii().getAmbulancies();
        List<Sestra> sestry = s.agentSestier().getSestry();
        List<Lekar> lekari = s.agentLekarov().getLekari();

        SwingUtilities.invokeLater(() -> {
            updateAmbulancieTable(ambulancie);
            updateSestryTable(sestry);
            updateLekariTable(lekari);
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

    private void updateAmbulancieTable(List<Ambulancia> ambulancie) {
        DefaultTableModel model = (DefaultTableModel) ambulancieTable.getModel();
        model.setRowCount(0);
        for (Ambulancia a : ambulancie) {
            model.addRow(new Object[]{
                a.id(),
                a.getTyp(),
                a.isJeObsadena() ? "OBSADENÁ" : "VOĽNÁ"
            });
        }
    }

    private void updateSestryTable(List<Sestra> sestry) {
        DefaultTableModel model = (DefaultTableModel) sestryTable.getModel();
        model.setRowCount(0);
        for (Sestra s : sestry) {
            String pacientInfo = s.getAktualnyPacient() != null
                ? "Pac. " + s.getAktualnyPacient().id()
                : "-";
            model.addRow(new Object[]{
                s.id(),
                s.jeObsadena() ? "OBSADENÁ" : "VOĽNÁ",
                pacientInfo
            });
        }
    }

    private void updateLekariTable(List<Lekar> lekari) {
        DefaultTableModel model = (DefaultTableModel) lekariTable.getModel();
        model.setRowCount(0);
        for (Lekar l : lekari) {
            String pacientInfo = l.getAktualnyPacient() != null
                ? "Pac. " + l.getAktualnyPacient().id()
                : "-";
            model.addRow(new Object[]{
                l.id(),
                l.jeObsadeny() ? "OBSADENÝ" : "VOĽNÝ",
                pacientInfo
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
}
