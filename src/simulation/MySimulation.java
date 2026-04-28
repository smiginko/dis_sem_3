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

import java.util.Random;

public class MySimulation extends OSPABA.Simulation
{
    private StrategiaPridelovania strategiaPridelovania = StrategiaPridelovania.PRVA_VOLNA;

    // 1.0 = 1x reálny čas, 0.1 = 10x, null = turbo
    private Double desiredSpeedDuration = null;

    private final java.util.List<String> logs = new java.util.ArrayList<>();

    Random seedGenerator;

    private int pocetAmbulanciiA = 5;
    private int pocetAmbulanciiB = 7;

    private int pocetSestier = 5;
    private int pocetLekarov = 4;

	public MySimulation()
	{
		init();
	}

	@Override
	public void prepareSimulation()
	{
		super.prepareSimulation();
		// Create global statistcis
        this.seedGenerator = new Random();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
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

    private String formatCas(double totalSeconds) {
        if (Double.isNaN(totalSeconds) || totalSeconds < 0) return "00:00:00";
        int total = (int) Math.round(totalSeconds);
        return String.format("%02d:%02d:%02d", total / 3600, (total % 3600) / 60, total % 60);
    }
}