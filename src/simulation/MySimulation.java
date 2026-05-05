package simulation;

import agents.agentlekarov.*;
import OSPABA.*;
import agents.agentosetrenia.*;
import agents.agentokolia.*;
import agents.agentsestier.*;
import agents.hlavnyagent.*;
import agents.agentvstupnehovysetrenia.*;
import agents.agenturgentu.*;
import agents.agentambulancii.*;
import simulation.animacia.AnimaciaUrgentu;
import statistiky.Statistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MySimulation extends OSPABA.Simulation
{

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

    private AnimaciaUrgentu animaciaUrgentu;
    private StrategiaPridelovania strategiaPridelovania = StrategiaPridelovania.PRVA_VOLNA;

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
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
        animaciaUrgentu.prepareReplication();
        logs.clear();

        if (desiredSpeedDuration != null) {
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

        ManagerOkolia managerOkolia = (ManagerOkolia) agentOkolia().myManager();
        ManagerUrgentu managerUrgentu = (ManagerUrgentu) agentUrgentu().myManager();
        ManagerAmbulancii managerAmbulancii = (ManagerAmbulancii) agentAmbulancii().myManager();
        ManagerSestier managerSestier = (ManagerSestier) agentSestier().myManager();
        ManagerLekarov managerLekarov = (ManagerLekarov) agentLekarov().myManager();

        if (managerOkolia.getCelkovyCasVSystemeStat().getCount() > 0) {
            globCelkovyCasVSysteme.addValue(managerOkolia.getCelkovyCasVSystemeStat().getAverage());
        }

        if (managerUrgentu.getCasVCakarniVstupnePesoStat().getCount() > 0) {
            globCasVCakarniVstupnePeso.addValue(
                    managerUrgentu.getCasVCakarniVstupnePesoStat().getAverage()
            );
        }

        if (managerUrgentu.getCasVCakarniVstupneSanitkaStat().getCount() > 0) {
            globCasVCakarniVstupneSanitka.addValue(
                    managerUrgentu.getCasVCakarniVstupneSanitkaStat().getAverage()
            );
        }

        for (int priorita = 1; priorita <= 5; priorita++) {
            Statistic lokalna = managerUrgentu.getCasVCakarniOsetreniePriorita(priorita);

            if (lokalna.getCount() > 0) {
                globCasVCakarniOsetreniePriorita[priorita].addValue(lokalna.getAverage());
            }
        }

        globDlzkaRaduVstupne.addValue(
                managerUrgentu.getDlzkaRaduVstupne().getAverage(currentTime())
        );

        globDlzkaRaduOsetrenie.addValue(
                managerUrgentu.getDlzkaRaduOsetrenie().getAverage(currentTime())
        );

        globVytazenieAmbulanciiA.addValue(
                managerAmbulancii.getVytazenieAmbulanciiA().getAverage(currentTime())
        );

        globVytazenieAmbulanciiB.addValue(
                managerAmbulancii.getVytazenieAmbulanciiB().getAverage(currentTime())
        );

        globVytazenieSestier.addValue(
                managerSestier.getVytazenieSestryStat().getAverage(currentTime())
        );

        globVytazenieLekarov.addValue(
                managerLekarov.getVytazenieLekarovStat().getAverage(currentTime())
        );

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

    public StrategiaPridelovania getStrategiaPridelovania() {
        return strategiaPridelovania;
    }

    public void setStrategiaPridelovania(StrategiaPridelovania strategiaPridelovania) {
        this.strategiaPridelovania = strategiaPridelovania;
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
        String formatted = String.format("[%s] %s", formatCas(currentTime()), message);
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
    public double getWarmupTime()           { return warmupTime; }
    public void setWarmupTime(double t)     { this.warmupTime = t; }

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

}