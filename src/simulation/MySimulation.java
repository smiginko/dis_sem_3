package simulation;

import agents.agentlekarov.*;
import agents.agentosetrenia.*;
import agents.agentokolia.*;
import agents.agentsestier.*;
import agents.hlavnyagent.*;
import agents.agentvstupnehovysetrenia.*;
import agents.agenturgentu.*;
import agents.agentambulancii.*;
import simulation.animacia.AnimaciaUrgentu;
import statistiky.Statistic;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MySimulation extends OSPABA.Simulation
{
    private static final double LIMIT_SANITKA_DO_OSETRENIA = 15 * 60.0;
    private static final double LIMIT_PESO_DO_OSETRENIA = 30 * 60.0;

    private Statistic globCasVCakarniVstupnePeso;
    private Statistic globCasVCakarniVstupneSanitka;
    private Statistic[] globCasVCakarniOsetreniePriorita = new Statistic[6];
    private Statistic globCelkovyCasVSysteme;
    private Statistic globDlzkaRaduVstupne;
    private Statistic globDlzkaRaduOsetrenie;
    private Statistic globVytazenieLekarov;
    private Statistic globVytazenieSestier;
    private Statistic globVytazenieAmbulanciiA;
    private Statistic globVytazenieAmbulanciiB;
    private Statistic globCasOdVstupuPoZaciatokOsetreniaSanitka;
    private Statistic globCasOdVstupuPoZaciatokOsetreniaPeso;

    private AnimaciaUrgentu animaciaUrgentu;
    private StrategiaAmbulancii strategiaAmbulancii = StrategiaAmbulancii.VYVAZENY_TYP_A_B_PERSONAL_PRI_AMBULANCII;
    private StrategiaSestier strategiaSestier = StrategiaSestier.SESTRA_V_AMBULANCII_INAK_PRVA_VOLNA;
    private StrategiaLekarov strategiaLekarov = StrategiaLekarov.LEKAR_V_AMBULANCII_INAK_PRVY_VOLNY;

    // 1.0 = 1x reálny čas, 0.1 = 10x, null = turbo
    private Double desiredSpeedDuration = null;

    private final java.util.List<String> logs = new java.util.ArrayList<>();

    private double warmupTime = 0;

    private boolean collectAnalysisData = false;
    private final List<Double> analysisTimes = new ArrayList<>();
    private final List<Double> analysisValues = new ArrayList<>();
    public static final double SNAPSHOT_INTERVAL = 300.0;

    Random seedGenerator;

    private int pocetAmbulanciiA = 5;
    private int pocetAmbulanciiB = 7;

    private int pocetSestier = 8;
    private int pocetLekarov = 6;

	public MySimulation()
	{
		init();
        this.animaciaUrgentu = new AnimaciaUrgentu(this);
	}

	@Override
	public void prepareSimulation()
	{
		super.prepareSimulation();
		// Create global statistcis
        this.seedGenerator = new Random();

        globCasVCakarniVstupnePeso = new Statistic("Priemerny cas v cakarni (peso)");
        globCasVCakarniVstupneSanitka =  new Statistic("Priemerny cas v cakarni (sanitka)");

        this.globCasVCakarniOsetreniePriorita = new Statistic[6];
        for (int priorita = 1; priorita <= 5; priorita++) {
            this.globCasVCakarniOsetreniePriorita[priorita] =
                    new Statistic("Cas v cakarni pred osetrenim - priorita " + priorita);
        }

        this.globCelkovyCasVSysteme = new Statistic("Celkovy cas pacienta v systeme");

        this.globDlzkaRaduVstupne = new Statistic("Priemerna dlzka radu na vstupne vysetrenie");
        this.globDlzkaRaduOsetrenie = new Statistic("Priemerna dlzka radu na osetrenie");

        this.globVytazenieLekarov = new Statistic("Vytazenie lekarov");
        this.globVytazenieSestier = new Statistic("Vytazenie sestier");
        this.globVytazenieAmbulanciiA = new Statistic("Vytazenie ambulancii typ A");
        this.globVytazenieAmbulanciiB = new Statistic("Vytazenie ambulancii typ B");
        globCasOdVstupuPoZaciatokOsetreniaSanitka = new Statistic("Cas od vstupu po zaciatok osetrenia - SANITKA");
        globCasOdVstupuPoZaciatokOsetreniaPeso = new Statistic("Cas od vstupu po zaciatok osetrenia - PESO");
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
        animaciaUrgentu.prepareReplication();
        logs.clear();

        if (warmupTime > 0) {
            setMaxSimSpeed();
        } else if (desiredSpeedDuration != null) {
            setSimSpeed(1.0, desiredSpeedDuration);
        } else {
            setMaxSimSpeed();
        }

        MyMessage message = new MyMessage(this);
        message.setCode(Mc.init);
        message.setAddressee(agentOkolia());

        hlavnyAgent().myManager().notice(message);
	}

	@Override
	public void replicationFinished()
	{
		// Collect local statistics into global, update UI, etc...

        AgentOkolia okolie = agentOkolia();
        AgentUrgentu urgent = agentUrgentu();
        AgentAmbulancii ambulancie = agentAmbulancii();
        AgentSestier sestry = agentSestier();
        AgentLekarov lekari = agentLekarov();

        if (okolie.getCelkovyCasVSystemeStat().getCount() > 0) {
            globCelkovyCasVSysteme.addValue(okolie.getCelkovyCasVSystemeStat().getAverage());
        }

        if (urgent.getCasVCakarniVstupnePesoStat().getCount() > 0) {
            globCasVCakarniVstupnePeso.addValue(
                    urgent.getCasVCakarniVstupnePesoStat().getAverage()
            );
        }

        if (urgent.getCasVCakarniVstupneSanitkaStat().getCount() > 0) {
            globCasVCakarniVstupneSanitka.addValue(
                    urgent.getCasVCakarniVstupneSanitkaStat().getAverage()
            );
        }

        for (int priorita = 1; priorita <= 5; priorita++) {
            Statistic lokalna = urgent.getCasVCakarniOsetreniePriorita(priorita);

            if (lokalna.getCount() > 0) {
                globCasVCakarniOsetreniePriorita[priorita].addValue(lokalna.getAverage());
            }
        }

        globDlzkaRaduVstupne.addValue(
                urgent.getDlzkaRaduVstupne().getAverage(currentTime())
        );

        globDlzkaRaduOsetrenie.addValue(
                urgent.getDlzkaRaduOsetrenie().getAverage(currentTime())
        );

        globVytazenieAmbulanciiA.addValue(
                ambulancie.getVytazenieAmbulanciiA().getAverage(currentTime())
        );

        globVytazenieAmbulanciiB.addValue(
                ambulancie.getVytazenieAmbulanciiB().getAverage(currentTime())
        );

        globVytazenieSestier.addValue(
                sestry.getVytazenieSestryStat().getAverage(currentTime())
        );

        globVytazenieLekarov.addValue(
                lekari.getVytazenieLekarovStat().getAverage(currentTime())
        );

        if (urgent.getCasOdVstupuPoZaciatokOsetreniaSanitkaStat().getCount() > 0) {
            globCasOdVstupuPoZaciatokOsetreniaSanitka.addValue(
                    urgent.getCasOdVstupuPoZaciatokOsetreniaSanitkaStat().getAverage()
            );
        }

        if (urgent.getCasOdVstupuPoZaciatokOsetreniaPesoStat().getCount() > 0) {
            globCasOdVstupuPoZaciatokOsetreniaPeso.addValue(
                    urgent.getCasOdVstupuPoZaciatokOsetreniaPesoStat().getAverage()
            );
        }

        super.replicationFinished();
    }

	@Override
	public void simulationFinished()
	{
		// Display simulation results
		super.simulationFinished();
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		setHlavnyAgent(new HlavnyAgent(Id.hlavnyAgent, this, null));
		setAgentOkolia(new AgentOkolia(Id.agentOkolia, this, hlavnyAgent()));
		setAgentUrgentu(new AgentUrgentu(Id.agentUrgentu, this, hlavnyAgent()));
		setAgentVstupnehoVysetrenia(new AgentVstupnehoVysetrenia(Id.agentVstupnehoVysetrenia, this, agentUrgentu()));
		setAgentOsetrenia(new AgentOsetrenia(Id.agentOsetrenia, this, agentUrgentu()));
		setAgentAmbulancii(new AgentAmbulancii(Id.agentAmbulancii, this, agentUrgentu()));
		setAgentSestier(new AgentSestier(Id.agentSestier, this, agentUrgentu()));
		setAgentLekarov(new AgentLekarov(Id.agentLekarov, this, agentUrgentu()));
	}

    /**
     *  Táto časť kódu vznikla pregenrovaním pomocou AI z riešenie z druhej semestrálnej práce
     */

    public static class PersonalExperimentResult {
        public final int pocetSestier;
        public final int pocetLekarov;
        public final double casSanitka;
        public final double casPeso;
        public final boolean found;

        public PersonalExperimentResult(int pocetSestier, int pocetLekarov,
                                        double casSanitka, double casPeso,
                                        boolean found) {
            this.pocetSestier = pocetSestier;
            this.pocetLekarov = pocetLekarov;
            this.casSanitka = casSanitka;
            this.casPeso = casPeso;
            this.found = found;
        }
    }

    public PersonalExperimentResult runPersonalExperiment(
            int minSestry,
            int maxSestry,
            int minLekari,
            int maxLekari,
            int reps,
            double simTime,
            double warmUp,
            int pocetAmbulanciiA,
            int pocetAmbulanciiB,
            StrategiaLekarov strategiaLekarov,
            StrategiaSestier strategiaSestier,
            StrategiaAmbulancii strategiaAmbulancii
    ) {
        return runPersonalExperiment(
                minSestry,
                maxSestry,
                minLekari,
                maxLekari,
                reps,
                simTime,
                warmUp,
                pocetAmbulanciiA,
                pocetAmbulanciiB,
                strategiaAmbulancii,
                strategiaSestier,
                strategiaLekarov
        );
    }

    public PersonalExperimentResult runPersonalExperiment(
            int minSestry,
            int maxSestry,
            int minLekari,
            int maxLekari,
            int reps,
            double simTime,
            double warmUp,
            int pocetAmbulanciiA,
            int pocetAmbulanciiB,
            StrategiaAmbulancii strategiaAmbulancii,
            StrategiaSestier strategiaSestier,
            StrategiaLekarov strategiaLekarov
    ) {
        File dir = new File("assets");
        if (!dir.exists()) {
            dir.mkdir();
        }

        try (PrintWriter pw = new PrintWriter(new File(dir, "experiment_personal.csv"))) {
            pw.println("Sestry;Lekari;Sanitka_do_osetrenia_s;Peso_do_osetrenia_s;Status");

            for (int total = minSestry + minLekari; total <= maxSestry + maxLekari; total++) {
                PersonalExperimentResult bestInLayer = null;

                for (int sestry = minSestry; sestry <= maxSestry; sestry++) {
                    int lekari = total - sestry;

                    if (lekari < minLekari || lekari > maxLekari) {
                        continue;
                    }

                    MySimulation test = new MySimulation();
                    test.setPocetAmbulanciiA(pocetAmbulanciiA);
                    test.setPocetAmbulanciiB(pocetAmbulanciiB);
                    test.setPocetSestier(sestry);
                    test.setPocetLekarov(lekari);
                    test.setWarmupTime(warmUp);
                    test.setStrategiaAmbulancii(strategiaAmbulancii);
                    test.setStrategiaSestier(strategiaSestier);
                    test.setStrategiaLekarov(strategiaLekarov);
                    test.setTurboDesired();

                    test.simulate(reps, simTime + warmUp);

                    Statistic sanitkaStat = test.getGlobCasOdVstupuPoZaciatokOsetreniaSanitka();
                    Statistic pesoStat = test.getGlobCasOdVstupuPoZaciatokOsetreniaPeso();

                    boolean hasData = sanitkaStat.getCount() > 0 && pesoStat.getCount() > 0;
                    double casSanitka = hasData ? sanitkaStat.getAverage() : Double.NaN;
                    double casPeso = hasData ? pesoStat.getAverage() : Double.NaN;

                    boolean ok = hasData
                            && casSanitka <= LIMIT_SANITKA_DO_OSETRENIA
                            && casPeso < LIMIT_PESO_DO_OSETRENIA;

                    pw.printf(Locale.US, "%d;%d;%.2f;%.2f;%s%n",
                            sestry,
                            lekari,
                            casSanitka,
                            casPeso,
                            ok ? "OK" : "FAIL"
                    );
                    pw.flush();

                    if (ok) {
                        PersonalExperimentResult candidate =
                                new PersonalExperimentResult(sestry, lekari, casSanitka, casPeso, true);

                        if (bestInLayer == null || personalExperimentScore(candidate) < personalExperimentScore(bestInLayer)) {
                            bestInLayer = candidate;
                        }
                    }
                }

                if (bestInLayer != null) {
                    return bestInLayer;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new PersonalExperimentResult(-1, -1, Double.NaN, Double.NaN, false);
    }

    private double personalExperimentScore(PersonalExperimentResult result) {
        return Math.max(
                result.casSanitka / LIMIT_SANITKA_DO_OSETRENIA,
                result.casPeso / LIMIT_PESO_DO_OSETRENIA
        );
    }

    /**
     * ------------------------------------------------
     */

    private HlavnyAgent _hlavnyAgent;

public HlavnyAgent hlavnyAgent()
	{ return _hlavnyAgent; }

	public void setHlavnyAgent(HlavnyAgent hlavnyAgent)
	{_hlavnyAgent = hlavnyAgent; }

	private AgentOkolia _agentOkolia;

public AgentOkolia agentOkolia()
	{ return _agentOkolia; }

	public void setAgentOkolia(AgentOkolia agentOkolia)
	{_agentOkolia = agentOkolia; }

	private AgentUrgentu _agentUrgentu;

public AgentUrgentu agentUrgentu()
	{ return _agentUrgentu; }

	public void setAgentUrgentu(AgentUrgentu agentUrgentu)
	{_agentUrgentu = agentUrgentu; }

	private AgentVstupnehoVysetrenia _agentVstupnehoVysetrenia;

public AgentVstupnehoVysetrenia agentVstupnehoVysetrenia()
	{ return _agentVstupnehoVysetrenia; }

	public void setAgentVstupnehoVysetrenia(AgentVstupnehoVysetrenia agentVstupnehoVysetrenia)
	{_agentVstupnehoVysetrenia = agentVstupnehoVysetrenia; }

	private AgentOsetrenia _agentOsetrenia;

public AgentOsetrenia agentOsetrenia()
	{ return _agentOsetrenia; }

	public void setAgentOsetrenia(AgentOsetrenia agentOsetrenia)
	{_agentOsetrenia = agentOsetrenia; }

	private AgentAmbulancii _agentAmbulancii;

public AgentAmbulancii agentAmbulancii()
	{ return _agentAmbulancii; }

	public void setAgentAmbulancii(AgentAmbulancii agentAmbulancii)
	{_agentAmbulancii = agentAmbulancii; }

	private AgentSestier _agentSestier;

public AgentSestier agentSestier()
	{ return _agentSestier; }

	public void setAgentSestier(AgentSestier agentSestier)
	{_agentSestier = agentSestier; }

	private AgentLekarov _agentLekarov;

public AgentLekarov agentLekarov()
	{ return _agentLekarov; }

	public void setAgentLekarov(AgentLekarov agentLekarov)
	{_agentLekarov = agentLekarov; }
	//meta! tag="end"

    public int getPocetAmbulanciiA() {
        return pocetAmbulanciiA;
    }

    public void setPocetAmbulanciiA(int pocetAmbulanciiA) {
        this.pocetAmbulanciiA = pocetAmbulanciiA;
    }

    public int getPocetAmbulanciiB() {
        return pocetAmbulanciiB;
    }

    public void setPocetAmbulanciiB(int pocetAmbulanciiB) {
        this.pocetAmbulanciiB = pocetAmbulanciiB;
    }

    public Random  getSeedGenerator() { return seedGenerator; }

    public StrategiaAmbulancii getStrategiaAmbulancii() {
        return strategiaAmbulancii;
    }

    public void setStrategiaAmbulancii(StrategiaAmbulancii strategiaAmbulancii) {
        this.strategiaAmbulancii = strategiaAmbulancii;
    }

    public StrategiaSestier getStrategiaSestier() {
        return strategiaSestier;
    }

    public void setStrategiaSestier(StrategiaSestier strategiaSestier) {
        this.strategiaSestier = strategiaSestier;
    }

    public StrategiaLekarov getStrategiaLekarov() {
        return strategiaLekarov;
    }

    public void setStrategiaLekarov(StrategiaLekarov strategiaLekarov) {
        this.strategiaLekarov = strategiaLekarov;
    }

    public int getPocetSestier() {
        return pocetSestier;
    }

    public void setPocetSestier(int pocetSestier) {
        this.pocetSestier = pocetSestier;
    }

    public int getPocetLekarov() {
        return pocetLekarov;
    }

    public void setPocetLekarov(int pocetLekarov) {
        this.pocetLekarov = pocetLekarov;
    }

    public void setDesiredSpeed(double durationSeconds) {
        this.desiredSpeedDuration = durationSeconds;
    }

    public void setTurboDesired() {
        this.desiredSpeedDuration = null;
    }

    public void log(String message) {
        if (isMaxSpeed()) return;
        String formatted = String.format("[%s] %s", formatCas(currentTime() - warmupTime), message);
        logs.add(formatted);
    }

    public java.util.List<String> getNewLogs() {
        java.util.List<String> copy = new java.util.ArrayList<>(logs);
        logs.clear();
        return copy;
    }

    public AnimaciaUrgentu animaciaUrgentu() {
        return animaciaUrgentu;
    }

    public void recordSnapshot(double value) {
        if (!collectAnalysisData) return;
        analysisTimes.add(currentTime());
        analysisValues.add(value);
    }

    public void clearAnalysisSnapshots() { analysisTimes.clear(); analysisValues.clear(); }
    public List<Double> getAnalysisTimes()  { return new ArrayList<>(analysisTimes); }
    public List<Double> getAnalysisValues() { return new ArrayList<>(analysisValues); }
    public boolean isCollectAnalysisData()  { return collectAnalysisData; }
    public void setCollectAnalysisData(boolean v) { collectAnalysisData = v; }
    public double getWarmupTime()             { return warmupTime; }
    public void setWarmupTime(double t)       { this.warmupTime = t; }
    public Double getDesiredSpeedDuration()   { return desiredSpeedDuration; }

    private volatile boolean warmupJustEnded = false;
    public void signalWarmupEnded()          { warmupJustEnded = true; }
    public boolean pollWarmupJustEnded() { boolean v = warmupJustEnded; warmupJustEnded = false; return v; }

    private String formatCas(double totalSeconds) {
        if (Double.isNaN(totalSeconds) || totalSeconds < 0) return "00:00:00";
        int total = (int) Math.round(totalSeconds);
        return String.format("%02d:%02d:%02d", total / 3600, (total % 3600) / 60, total % 60);
    }

    public Statistic getGlobCasVCakarniVstupnePeso() { return globCasVCakarniVstupnePeso; }
    public Statistic getGlobCasVCakarniVstupneSanitka() { return globCasVCakarniVstupneSanitka; }
    public Statistic getGlobCasVCakarniOsetreniePriorita(int priorita) { return globCasVCakarniOsetreniePriorita[priorita]; }
    public Statistic getGlobCelkovyCasVSysteme() { return globCelkovyCasVSysteme; }
    public Statistic getGlobDlzkaRaduVstupne() { return globDlzkaRaduVstupne; }
    public Statistic getGlobDlzkaRaduOsetrenie() { return globDlzkaRaduOsetrenie; }
    public Statistic getGlobVytazenieLekarov() { return globVytazenieLekarov; }
    public Statistic getGlobVytazenieSestier() { return globVytazenieSestier; }
    public Statistic getGlobVytazenieAmbulanciiA() { return globVytazenieAmbulanciiA; }
    public Statistic getGlobVytazenieAmbulanciiB() { return globVytazenieAmbulanciiB; }
    public Statistic getGlobCasOdVstupuPoZaciatokOsetreniaSanitka() { return globCasOdVstupuPoZaciatokOsetreniaSanitka; }
    public Statistic getGlobCasOdVstupuPoZaciatokOsetreniaPeso() { return globCasOdVstupuPoZaciatokOsetreniaPeso; }

}
