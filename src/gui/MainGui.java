package gui;

import OSPABA.ISimDelegate;
import OSPABA.SimState;
import OSPABA.Simulation;
import simulation.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MainGui extends JFrame implements ISimDelegate {
    private JPanel animaciaContainer;

    private MySimulation sim;

    private JLabel timeLabel;
    private JLabel repLabel;
    private JSlider speedSlider;
    private JTextArea logArea;
    private JButton startBtn;
    private JButton stopBtn;
    private JButton pauseBtn;
    private JButton resumeBtn;
    private JButton warmupBtn;
    private JButton personalExperimentBtn;
    private JCheckBox turboCb;
    private boolean finalDialogShown = false;
    private boolean turboRequested = false;
    private int selectedSpeedIndex = 0;
    private JTextArea statistikyArea;

    private static final int[] SPEED_MULTIPLIERS = {1, 2, 3, 5, 10, 50, 100, 1000};

    private JTable vstupneTable;
    private JTable osetreniTable;
    private JTable ambulancieTable;
    private JTable sestryTable;
    private JTable lekariTable;

    private GrafyUstalovaniObserver grafyObserver;

    public MainGui() {
        this.sim = new MySimulation();
        this.grafyObserver = new GrafyUstalovaniObserver();
        setTitle("Simulácia Urgentného Príjmu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        initLayout();

        sim.registerDelegate(new FrontyObserver(vstupneTable, osetreniTable));
        sim.registerDelegate(new ZdrojeObserver(ambulancieTable, sestryTable, lekariTable));
        sim.registerDelegate(new StatistikyObserver(statistikyArea));
        sim.registerDelegate(this);
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        // --- NORTH: kontrolný panel ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        startBtn = new JButton("Štart");
        pauseBtn = new JButton("Pauza");
        resumeBtn = new JButton("Pokračovať");
        warmupBtn = new JButton("Warm-up analýza");
        stopBtn = new JButton("Stop");
        pauseBtn.setEnabled(false);
        resumeBtn.setEnabled(false);
        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> showStartDialog());

        warmupBtn.addActionListener(e -> {
            WarmUpAnalysisFrame f = new WarmUpAnalysisFrame();
            f.setVisible(true);
        });

        personalExperimentBtn = new JButton("Experiment personalu");
        personalExperimentBtn.addActionListener(e -> showPersonalExperimentDialog());

        pauseBtn.addActionListener(e -> sim.pauseSimulation());
        resumeBtn.addActionListener(e -> sim.resumeSimulation());
        stopBtn.addActionListener(e -> {
            sim.stopSimulation();
            setRunningControls(false);
        });

        speedSlider = new JSlider(0, SPEED_MULTIPLIERS.length - 1, 0);
        speedSlider.setPreferredSize(new Dimension(280, 45));
        speedSlider.setPaintLabels(true);
        java.util.Hashtable<Integer, JLabel> labels = new java.util.Hashtable<>();
        for (int i = 0; i < SPEED_MULTIPLIERS.length; i++) {
            labels.put(i, new JLabel(SPEED_MULTIPLIERS[i] + "x"));
        }
        speedSlider.setLabelTable(labels);
        speedSlider.addChangeListener(e -> {
            selectedSpeedIndex = speedSlider.getValue();

            if (!turboRequested && sim != null) {
                double duration = selectedDuration();
                sim.setDesiredSpeed(duration);

                if (sim.isRunning()) {
                    sim.setSimSpeed(1.0, duration);
                }
            }
        });

        turboCb = new JCheckBox("TURBO");
        turboCb.addActionListener(e -> {
            turboRequested = turboCb.isSelected();

            if (turboRequested) {
                sim.setTurboDesired();
                sim.setMaxSimSpeed();
                timeLabel.setText("--- TURBO ---");
                timeLabel.setForeground(Color.BLUE);
            } else {
                double duration = selectedDuration();
                sim.setDesiredSpeed(duration);

                if (sim.isRunning()) {
                    sim.setSimSpeed(1.0, duration);
                }

                timeLabel.setForeground(Color.BLACK);
            }

            speedSlider.setEnabled(!turboRequested);
        });

        timeLabel = new JLabel("00:00:00");
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        repLabel = new JLabel("Rep: 0");
        repLabel.setFont(new Font("Monospaced", Font.BOLD, 20));

        topBar.add(startBtn);
        topBar.add(pauseBtn);
        topBar.add(resumeBtn);
        topBar.add(stopBtn);
        topBar.add(warmupBtn);
        topBar.add(personalExperimentBtn);
        topBar.add(new JSeparator(JSeparator.VERTICAL));
        topBar.add(turboCb);
        topBar.add(new JLabel("Rýchlosť:"));
        topBar.add(speedSlider);
        topBar.add(new JSeparator(JSeparator.VERTICAL));
        topBar.add(repLabel);
        topBar.add(timeLabel);
        add(topBar, BorderLayout.NORTH);

        // --- CENTER: záložky ---
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Simulácia", buildSimulationTab());
        tabbedPane.addTab("Animácia", buildAnimaciaTab());

        add(tabbedPane, BorderLayout.CENTER);

        // --- SOUTH: log ---
        logArea = new JTextArea(5, 0);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("LOG"));
        add(logScroll, BorderLayout.SOUTH);
    }

    private JPanel buildSimulationTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // CENTER: fronty
        JPanel centerPanel = new JPanel(new BorderLayout(5, 10));
        JPanel frontyPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        frontyPanel.setPreferredSize(new Dimension(0, 210));
        frontyPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        vstupneTable = createTable();
        osetreniTable = createTable();

        statistikyArea = new JTextArea();
        statistikyArea.setEditable(false);
        statistikyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statistikyArea.setBackground(new Color(250, 250, 250));

        frontyPanel.add(createTableWrapper(vstupneTable,
            "FRONT – Vstupné vyšetrenie", "#", "PacientID", "Typ", "Stav", "Príchod"));
        frontyPanel.add(createTableWrapper(osetreniTable,
            "FRONT – Ošetrenie", "#", "PacientID", "Typ", "Priorita", "Stav"));

        JScrollPane statistikyScroll = new JScrollPane(statistikyArea);
        statistikyScroll.setBorder(BorderFactory.createTitledBorder("ŠTATISTIKY"));

        centerPanel.add(frontyPanel, BorderLayout.NORTH);

        JPanel midPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        midPanel.add(statistikyScroll);
        JPanel grafyPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        grafyPanel.add(grafyObserver.getPesoPanel());
        grafyPanel.add(grafyObserver.getSanitkaPanel());
        midPanel.add(grafyPanel);

        centerPanel.add(midPanel, BorderLayout.CENTER);
        // EAST: zdroje
        JPanel zdrojePanel = new JPanel(new GridLayout(3, 1, 5, 10));
        zdrojePanel.setPreferredSize(new Dimension(380, 0));

        ambulancieTable = createTable();
        sestryTable = createTable();
        lekariTable = createTable();

        zdrojePanel.add(createTableWrapper(ambulancieTable,
            "AMBULANCIE", "ID", "Typ", "Stav"));
        zdrojePanel.add(createTableWrapper(sestryTable,
            "SESTRY", "ID", "Stav", "Pacient"));
        zdrojePanel.add(createTableWrapper(lekariTable,
            "LEKÁRI", "ID", "Stav", "Pacient"));

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(zdrojePanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildAnimaciaTab() {
        animaciaContainer = new JPanel(new BorderLayout());
        animaciaContainer.add(new JLabel("Animácia sa vytvorí po štarte simulácie.", SwingConstants.CENTER),
                BorderLayout.CENTER);
        return animaciaContainer;
    }

    private JPanel createTableWrapper(JTable table, String title, String... cols) {
        ((DefaultTableModel) table.getModel()).setColumnIdentifiers(cols);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));

        JButton expandBtn = new JButton("↗");
        expandBtn.setMargin(new Insets(0, 1, 0, 1));
        expandBtn.setFont(new Font("Dialog", Font.PLAIN, 10));
        expandBtn.addActionListener(e -> {
            JFrame f = new JFrame(title);
            f.setSize(400, 500);
            f.add(new JScrollPane(new JTable(table.getModel())));
            f.setVisible(true);
        });
        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        header.add(expandBtn);

        p.add(header, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JTable createTable() {
        JTable t = new JTable(new DefaultTableModel());
        t.setPreferredScrollableViewportSize(new Dimension(250, 120));
        t.setFont(new Font("SansSerif", Font.PLAIN, 11));
        t.setRowHeight(18);
        t.setEnabled(false);
        return t;
    }

    private void showStartDialog() {
        JSpinner repSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, 100000, 1));
        JSpinner casSpinner = new JSpinner(new SpinnerNumberModel(2_419_200, 60, 10_419_200, 60));
        JSpinner warmupSpinner = new JSpinner(new SpinnerNumberModel(100_000, 0, 10_000_000, 3600));
        JSpinner ambASpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        JSpinner ambBSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 50, 1));
        JSpinner sestrySpinner = new JSpinner(new SpinnerNumberModel(8, 1, 50, 1));
        JSpinner lekariSpinner = new JSpinner(new SpinnerNumberModel(6, 1, 50, 1));
        JComboBox<StrategiaAmbulancii> strategiaBox =
            new JComboBox<>(StrategiaAmbulancii.values());
        JComboBox<StrategiaSestier> strategiaSestierBox =
            new JComboBox<>(StrategiaSestier.values());
        JComboBox<StrategiaLekarov> strategiaLekarovBox =
            new JComboBox<>(StrategiaLekarov.values());

        JPanel panel = new JPanel(new GridLayout(10, 2, 10, 5));
        panel.add(new JLabel("Replikácie:")); panel.add(repSpinner);
        panel.add(new JLabel("Čas simulácie (s):")); panel.add(casSpinner);
        panel.add(new JLabel("Zahrievanie (s, 0=vyp):")); panel.add(warmupSpinner);
        panel.add(new JLabel("Ambulancie Typ A:")); panel.add(ambASpinner);
        panel.add(new JLabel("Ambulancie Typ B:")); panel.add(ambBSpinner);
        panel.add(new JLabel("Sestry:")); panel.add(sestrySpinner);
        panel.add(new JLabel("Lekári:")); panel.add(lekariSpinner);
        panel.add(new JLabel("Strategia ambulancii:")); panel.add(strategiaBox);

        panel.add(new JLabel("Strategia sestier:")); panel.add(strategiaSestierBox);
        panel.add(new JLabel("Strategia lekarov:")); panel.add(strategiaLekarovBox);

        if (JOptionPane.showConfirmDialog(this, panel, "Nastavenia simulácie",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        finalDialogShown = false;
        grafyObserver.reset();
        timeLabel.setText("NAČÍTAVANIE...");
        timeLabel.setForeground(Color.RED);
        logArea.setText("");

        sim = new MySimulation();
        sim.setPocetAmbulanciiA((int) ambASpinner.getValue());
        sim.setPocetAmbulanciiB((int) ambBSpinner.getValue());
        sim.setWarmupTime(((Number) warmupSpinner.getValue()).doubleValue());
        sim.setPocetSestier((int) sestrySpinner.getValue());
        sim.setPocetLekarov((int) lekariSpinner.getValue());
        sim.setStrategiaAmbulancii((StrategiaAmbulancii) strategiaBox.getSelectedItem());
        sim.setStrategiaSestier((StrategiaSestier) strategiaSestierBox.getSelectedItem());
        sim.setStrategiaLekarov((StrategiaLekarov) strategiaLekarovBox.getSelectedItem());

        pripravAnimaciuPreSimulaciu();

        if (turboRequested) {
            sim.setTurboDesired();
            timeLabel.setText("--- TURBO ---");
            timeLabel.setForeground(Color.BLUE);
        } else {
            sim.setDesiredSpeed(selectedDuration());
            timeLabel.setForeground(Color.BLACK);
        }

        sim.registerDelegate(new FrontyObserver(vstupneTable, osetreniTable));
        sim.registerDelegate(new ZdrojeObserver(ambulancieTable, sestryTable, lekariTable));
        sim.registerDelegate(new StatistikyObserver(statistikyArea));
        sim.registerDelegate(grafyObserver);
        sim.registerDelegate(this);

        setRunningControls(true);

        int repCount = (int) repSpinner.getValue();
        double simTime = ((Number) casSpinner.getValue()).doubleValue() + sim.getWarmupTime();
        new Thread(() -> {
            sim.simulate(repCount, simTime);
            SwingUtilities.invokeLater(() -> setRunningControls(false));
        }).start();
    }

    @Override
    public void refresh(Simulation simulation) {
        MySimulation s = (MySimulation) simulation;

        if (s.isMaxSpeed()) {
            return;
        }

        boolean warmupKoncil = s.pollWarmupJustEnded();
        java.util.List<String> newLogs = s.getNewLogs();

        SwingUtilities.invokeLater(() -> {
            timeLabel.setText(formatCas(s.currentTime() - s.getWarmupTime()));
            timeLabel.setForeground(Color.BLACK);
            repLabel.setText("Rep: " + s.currentReplication());

            if (warmupKoncil) {
                logArea.setText("");
            }

            for (String log : newLogs) logArea.append(log + "\n");
            if (!newLogs.isEmpty()) logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void simStateChanged(Simulation simulation, SimState state) {
        MySimulation s = (MySimulation) simulation;
        SwingUtilities.invokeLater(() -> {
            if (state == SimState.replicationStopped) {
                repLabel.setText("Rep: " + s.currentReplication());
            }

            if (state == SimState.stopped) {
                timeLabel.setText("SKONČENÉ");
                timeLabel.setForeground(new Color(0, 150, 0));
                repLabel.setText("Rep: " + s.currentReplication());
                if (!finalDialogShown) {
                    finalDialogShown = true;
                    JOptionPane.showMessageDialog(this, "Simulácia bola ukončená.");
                }
            }
        });
    }

    private void setRunningControls(boolean running) {
        startBtn.setEnabled(!running);
        stopBtn.setEnabled(running);
        pauseBtn.setEnabled(running);
        resumeBtn.setEnabled(running);
        personalExperimentBtn.setEnabled(!running);
        turboCb.setEnabled(true);
        speedSlider.setEnabled(!running || !turboCb.isSelected());
    }

    private String formatCas(double totalSeconds) {
        if (Double.isNaN(totalSeconds) || totalSeconds < 0) return "00:00:00";
        int total = (int) Math.round(totalSeconds);
        return String.format("%02d:%02d:%02d", total / 3600, (total % 3600) / 60, total % 60);
    }

    private double selectedDuration() {
        return 1.0 / SPEED_MULTIPLIERS[selectedSpeedIndex];
    }

    private void pripravAnimaciuPreSimulaciu() {
        animaciaContainer.removeAll();

        if (turboRequested) {
            animaciaContainer.add(new JLabel("Animácia je vypnutá v TURBO režime.", SwingConstants.CENTER),
                    BorderLayout.CENTER);
        } else {
            sim.createAnimator();
            sim.animator().setSynchronizedTime(true);
            animaciaContainer.add(sim.animator().canvas(), BorderLayout.CENTER);
        }

        animaciaContainer.revalidate();
        animaciaContainer.repaint();
    }

    /**
     * Táto časť kódu bola pregenrovaná pomocou AI z riešenia v druhej semestrálnej práce
     */
    private void showPersonalExperimentDialog() {
        JSpinner minSestrySpinner = new JSpinner(new SpinnerNumberModel(4, 1, 50, 1));
        JSpinner maxSestrySpinner = new JSpinner(new SpinnerNumberModel(14, 1, 50, 1));
        JSpinner minLekariSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
        JSpinner maxLekariSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 50, 1));
        JSpinner repSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 100000, 1));
        JSpinner casSpinner = new JSpinner(new SpinnerNumberModel(2_419_200, 60, 10_419_200, 60));
        JSpinner warmupSpinner = new JSpinner(new SpinnerNumberModel(100_000, 0, 10_000_000, 3600));
        JSpinner ambASpinner = new JSpinner(new SpinnerNumberModel(sim.getPocetAmbulanciiA(), 1, 50, 1));
        JSpinner ambBSpinner = new JSpinner(new SpinnerNumberModel(sim.getPocetAmbulanciiB(), 1, 50, 1));
        JComboBox<StrategiaAmbulancii> strategiaBox =
                new JComboBox<>(StrategiaAmbulancii.values());
        JComboBox<StrategiaSestier> strategiaSestierBox =
                new JComboBox<>(StrategiaSestier.values());
        JComboBox<StrategiaLekarov> strategiaLekarovBox =
                new JComboBox<>(StrategiaLekarov.values());
        strategiaBox.setSelectedItem(sim.getStrategiaAmbulancii());
        strategiaSestierBox.setSelectedItem(sim.getStrategiaSestier());
        strategiaLekarovBox.setSelectedItem(sim.getStrategiaLekarov());

        JPanel panel = new JPanel(new GridLayout(12, 2, 10, 5));
        panel.add(new JLabel("Min sestry:")); panel.add(minSestrySpinner);
        panel.add(new JLabel("Max sestry:")); panel.add(maxSestrySpinner);
        panel.add(new JLabel("Min lekari:")); panel.add(minLekariSpinner);
        panel.add(new JLabel("Max lekari:")); panel.add(maxLekariSpinner);
        panel.add(new JLabel("Replikacie:")); panel.add(repSpinner);
        panel.add(new JLabel("Cas simulacie (s):")); panel.add(casSpinner);
        panel.add(new JLabel("Warm-up (s):")); panel.add(warmupSpinner);
        panel.add(new JLabel("Ambulancie A:")); panel.add(ambASpinner);
        panel.add(new JLabel("Ambulancie B:")); panel.add(ambBSpinner);
        panel.add(new JLabel("Strategia ambulancii:")); panel.add(strategiaBox);
        panel.add(new JLabel("Strategia sestier:")); panel.add(strategiaSestierBox);
        panel.add(new JLabel("Strategia lekarov:")); panel.add(strategiaLekarovBox);

        if (JOptionPane.showConfirmDialog(this, panel, "Experiment personalu",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        int minSestry = (int) minSestrySpinner.getValue();
        int maxSestry = (int) maxSestrySpinner.getValue();
        int minLekari = (int) minLekariSpinner.getValue();
        int maxLekari = (int) maxLekariSpinner.getValue();

        if (minSestry > maxSestry || minLekari > maxLekari) {
            JOptionPane.showMessageDialog(this, "Minimalna hodnota nemoze byt vacsia ako maximalna.");
            return;
        }

        personalExperimentBtn.setEnabled(false);

        new SwingWorker<MySimulation.PersonalExperimentResult, Void>() {
            @Override
            protected MySimulation.PersonalExperimentResult doInBackground() {
                MySimulation experimentSim = new MySimulation();

                return experimentSim.runPersonalExperiment(
                        minSestry,
                        maxSestry,
                        minLekari,
                        maxLekari,
                        (int) repSpinner.getValue(),
                        ((Number) casSpinner.getValue()).doubleValue(),
                        ((Number) warmupSpinner.getValue()).doubleValue(),
                        (int) ambASpinner.getValue(),
                        (int) ambBSpinner.getValue(),
                        (StrategiaAmbulancii) strategiaBox.getSelectedItem(),
                        (StrategiaSestier) strategiaSestierBox.getSelectedItem(),
                        (StrategiaLekarov) strategiaLekarovBox.getSelectedItem()
                );
            }

            @Override
            protected void done() {
                personalExperimentBtn.setEnabled(true);

                try {
                    MySimulation.PersonalExperimentResult result = get();

                    if (result.found) {
                        JOptionPane.showMessageDialog(MainGui.this,
                                "Odporucany pocet sestier: " + result.pocetSestier + "\n"
                                        + "Odporucany pocet lekarov: " + result.pocetLekarov + "\n"
                                        + "SANITKA: " + formatCas(result.casSanitka) + "\n"
                                        + "PESO: " + formatCas(result.casPeso) + "\n\n"
                                        + "CSV: assets/experiment_personal.csv");
                    } else {
                        JOptionPane.showMessageDialog(MainGui.this,
                                "V zadanom rozsahu sa nenasla vyhovujuca kombinacia.\n"
                                        + "CSV: assets/experiment_personal.csv");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MainGui.this, "Experiment skoncil chybou.");
                }
            }
        }.execute();
    }
}
