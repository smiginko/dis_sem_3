package gui;

import OSPABA.ISimDelegate;
import OSPABA.SimState;
import OSPABA.Simulation;
import entity.Lekar;
import entity.Pacient;
import entity.Sestra;
import simulation.MyMessage;
import simulation.MySimulation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class FrontyObserver implements ISimDelegate {

    private static class VstupneRow {
        final int poradie;
        final int pacientId;
        final String typ;
        final String stav;
        final String prichod;

        VstupneRow(int poradie, int pacientId, String typ, String stav, String prichod) {
            this.poradie = poradie;
            this.pacientId = pacientId;
            this.typ = typ;
            this.stav = stav;
            this.prichod = prichod;
        }
    }

    private static class OsetrenieRow {
        final int poradie;
        final int pacientId;
        final String typ;
        final int priorita;
        final String stav;

        OsetrenieRow(int poradie, int pacientId, String typ, int priorita, String stav) {
            this.poradie = poradie;
            this.pacientId = pacientId;
            this.typ = typ;
            this.priorita = priorita;
            this.stav = stav;
        }
    }


    private final JTable vstupneTable;
    private final JTable osetreniTable;

    public FrontyObserver(JTable vstupneTable, JTable osetreniTable) {
        this.vstupneTable = vstupneTable;
        this.osetreniTable = osetreniTable;
    }

    @Override
    public void refresh(Simulation sim) {
        if (sim.isMaxSpeed()) return;

        MySimulation s = (MySimulation) sim;

        List<VstupneRow> vstupneSnapshot = vytvorVstupneSnapshot(s);
        List<OsetrenieRow> osetrenieSnapshot = vytvorOsetrenieSnapshot(s);

        SwingUtilities.invokeLater(() -> {
            updateVstupneTable(vstupneSnapshot);
            updateOsetreniTable(osetrenieSnapshot);
        });
    }

    @Override
    public void simStateChanged(Simulation sim, SimState state) {
        if (state == SimState.replicationRunning) {
            SwingUtilities.invokeLater(() -> {
                clearTable(vstupneTable);
                clearTable(osetreniTable);
            });
        }
    }

    private void updateVstupneTable(List<VstupneRow> rows) {
        DefaultTableModel model = (DefaultTableModel) vstupneTable.getModel();
        model.setRowCount(0);

        for (VstupneRow row : rows) {
            model.addRow(new Object[]{
                    row.poradie,
                    row.pacientId,
                    row.typ,
                    row.stav,
                    row.prichod
            });
        }
    }

    private void updateOsetreniTable(List<OsetrenieRow> rows) {
        DefaultTableModel model = (DefaultTableModel) osetreniTable.getModel();
        model.setRowCount(0);

        for (OsetrenieRow row : rows) {
            model.addRow(new Object[]{
                    row.poradie,
                    row.pacientId,
                    row.typ,
                    row.priorita,
                    row.stav
            });
        }
    }

    private List<VstupneRow> vytvorVstupneSnapshot(MySimulation s) {
        List<VstupneRow> rows = new java.util.ArrayList<>();
        int index = 1;

        for (MyMessage msg : s.agentUrgentu().getRadNaVstupneVysetrenie()) {
            rows.add(new VstupneRow(
                    index++,
                    msg.getPacient().id(),
                    msg.getPacient().getTyp().toString(),
                    "Čaká v čakárni",
                    String.format("%.1f s", msg.getPacient().getCasPrichodu())
            ));
        }

        for (MyMessage msg : s.agentAmbulancii().getRadCakajucich()) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE) {
                rows.add(new VstupneRow(
                        index++,
                        msg.getPacient().id(),
                        msg.getPacient().getTyp().toString(),
                        "Čaká na ambulanciu",
                        String.format("%.1f s", msg.getPacient().getCasPrichodu())
                ));
            }
        }

        for (MyMessage msg : s.agentSestier().getRadCakajucich()) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE) {
                rows.add(new VstupneRow(
                        index++,
                        msg.getPacient().id(),
                        msg.getPacient().getTyp().toString(),
                        "Čaká na sestru",
                        String.format("%.1f s", msg.getPacient().getCasPrichodu())
                ));
            }
        }

        return rows;
    }

    private List<OsetrenieRow> vytvorOsetrenieSnapshot(MySimulation s) {
        List<OsetrenieRow> rows = new java.util.ArrayList<>();
        int index = 1;

        for (MyMessage msg : s.agentUrgentu().getRadNaOsetrenie()) {
            rows.add(new OsetrenieRow(
                    index++,
                    msg.getPacient().id(),
                    msg.getPacient().getTyp().toString(),
                    msg.getPacient().getPriorita(),
                    "Čaká na pridelenie"
            ));
        }

        for (MyMessage msg : s.agentAmbulancii().getRadCakajucich()) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
                rows.add(new OsetrenieRow(
                        index++,
                        msg.getPacient().id(),
                        msg.getPacient().getTyp().toString(),
                        msg.getPacient().getPriorita(),
                        "Čaká na ambulanciu"
                ));
            }
        }

        for (MyMessage msg : s.agentLekarov().getRadCakajucich()) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
                rows.add(new OsetrenieRow(
                        index++,
                        msg.getPacient().id(),
                        msg.getPacient().getTyp().toString(),
                        msg.getPacient().getPriorita(),
                        "Čaká na lekára"
                ));
            }
        }

        for (MyMessage msg : s.agentSestier().getRadCakajucich()) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
                rows.add(new OsetrenieRow(
                        index++,
                        msg.getPacient().id(),
                        msg.getPacient().getTyp().toString(),
                        msg.getPacient().getPriorita(),
                        "Čaká na sestru"
                ));
            }
        }

        return rows;
    }


    private void clearTable(JTable table) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
    }
}
