package agents.agentosetrenia;

import OSPABA.*;
import agents.agentosetrenia.continualassistants.*;
import simulation.*;

//meta! id="24"
public class AgentOsetrenia extends OSPABA.Agent
{
	public AgentOsetrenia(int id, Simulation mySim, Agent parent)
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
		new ManagerOsetrenia(Id.managerOsetrenia, mySim(), this);
		new OdchodPacienta(Id.odchodPacienta, mySim(), this);
		new PresunDoAmbulancie(Id.presunDoAmbulancie, mySim(), this);
		new VykonanieOsetrenia(Id.vykonanieOsetrenia, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.osetreniePacienta);
	}
	//meta! tag="end"
}
