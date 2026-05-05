package agents.agentvstupnehovysetrenia;

import agents.agentvstupnehovysetrenia.instantassistants.*;
import OSPABA.*;
import agents.agentvstupnehovysetrenia.continualassistants.*;
import simulation.*;

//meta! id="19"
public class AgentVstupnehoVysetrenia extends OSPABA.Agent
{
	public AgentVstupnehoVysetrenia(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerVstupnehoVysetrenia(Id.managerVstupnehoVysetrenia, mySim(), this);
		new PrideleniePriority(Id.prideleniePriority, mySim(), this);
		new PresunNaVstupnuKontrolu(Id.presunNaVstupnuKontrolu, mySim(), this);
		new VykonanieVysetrenia(Id.vykonanieVysetrenia, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.vstupneVysetreniePacienta);
	}
	//meta! tag="end"
}