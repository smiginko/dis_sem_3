package agents.agentokolia;

import OSPABA.*;
import simulation.*;
import agents.agentokolia.continualassistants.*;
import statistiky.Statistic;

//meta! id="3"
public class AgentOkolia extends OSPABA.Agent
{
    private Statistic celkovyCasVSystemeStat;

	public AgentOkolia(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        celkovyCasVSystemeStat = new Statistic("Celkovy cas pacienta v systeme");
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerOkolia(Id.managerOkolia, mySim(), this);
		new ZachrankaScheduler(Id.zachrankaScheduler, mySim(), this);
		new PesoScheduler(Id.pesoScheduler, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.odchodPacienta);
		addOwnMessage(Mc.koniecZahrievania);
	}
	//meta! tag="end"

    public void resetStatistikyPoZahrievani() {
        celkovyCasVSystemeStat.reset();
    }

    public void zapisCelkovyCasVSysteme(MyMessage msg) {
        celkovyCasVSystemeStat.addValue(mySim().currentTime() - msg.getPacient().getCasPrichodu());
    }

    public Statistic getCelkovyCasVSystemeStat() {
        return celkovyCasVSystemeStat;
    }
}