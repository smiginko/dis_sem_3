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

        List<MyMessage> vstupneUrgent = s.agentUrgentu().getRadNaVstupneVysetrenie();
        List<MyMessage> osetrenieUrgent = s.agentUrgentu().getRadNaOsetrenie();
        
        List<MyMessage> ambulancieRad = s.agentAmbulancii().getRadCakajucich();
        List<MyMessage> sestryRad = s.agentSestier().getRadCakajucich();
        List<MyMessage> lekariRad = s.agentLekarov().getRadCakajucich();
        
        List<Sestra> sestry = s.agentSestier().getSestry();
        List<Lekar> lekari = s.agentLekarov().getLekari();

        SwingUtilities.invokeLater(() -> {
            updateVstupneTable(vstupneUrgent, ambulancieRad, sestryRad, sestry);
            updateOsetreniTable(osetrenieUrgent, ambulancieRad, lekariRad, sestryRad, lekari);
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

    private void updateVstupneTable(List<MyMessage> urgent, List<MyMessage> ambRad, List<MyMessage> sestryRad, List<Sestra> sestry) {
        DefaultTableModel model = (DefaultTableModel) vstupneTable.getModel();
        model.setRowCount(0);
        int index = 1;
        
        // 1. Urgentny rad
        for (MyMessage msg : urgent) {
            Pacient p = msg.getPacient();
            model.addRow(new Object[]{
                index++, p.id(), p.getTyp(), "Čaká v čakárni",
                String.format("%.1f s", p.getCasPrichodu())
            });
        }
        
        // 2. Rad na ambulanciu
        for (MyMessage msg : ambRad) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE) {
                Pacient p = msg.getPacient();
                model.addRow(new Object[]{
                    index++, p.id(), p.getTyp(), "Čaká na ambulanciu",
                    String.format("%.1f s", p.getCasPrichodu())
                });
            }
        }
        
        // 3. Rad na sestru
        for (MyMessage msg : sestryRad) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE) {
                Pacient p = msg.getPacient();
                model.addRow(new Object[]{
                    index++, p.id(), p.getTyp(), "Čaká na sestru",
                    String.format("%.1f s", p.getCasPrichodu())
                });
            }
        }
    }

    private void updateOsetreniTable(List<MyMessage> urgent, List<MyMessage> ambRad, List<MyMessage> lekariRad, List<MyMessage> sestryRad, List<Lekar> lekari) {
        DefaultTableModel model = (DefaultTableModel) osetreniTable.getModel();
        model.setRowCount(0);
        int index = 1;
        
        // 1. Urgentny rad
        for (MyMessage msg : urgent) {
            Pacient p = msg.getPacient();
            model.addRow(new Object[]{
                index++, p.id(), p.getTyp(), p.getPriorita(), "Čaká na pridelenie"
            });
        }
        
        // 2. Rad na ambulanciu
        for (MyMessage msg : ambRad) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
                Pacient p = msg.getPacient();
                model.addRow(new Object[]{
                    index++, p.id(), p.getTyp(), p.getPriorita(), "Čaká na ambulanciu"
                });
            }
        }
        
        // 3. Rad na lekára
        for (MyMessage msg : lekariRad) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
                Pacient p = msg.getPacient();
                model.addRow(new Object[]{
                    index++, p.id(), p.getTyp(), p.getPriorita(), "Čaká na lekára"
                });
            }
        }
        
        // 4. Rad na sestru
        for (MyMessage msg : sestryRad) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
                Pacient p = msg.getPacient();
                model.addRow(new Object[]{
                    index++, p.id(), p.getTyp(), p.getPriorita(), "Čaká na sestru"
                });
            }
        }
    }

    private void clearTable(JTable table) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
    }
}
