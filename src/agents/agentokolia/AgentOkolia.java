package agents.agentokolia;

import OSPABA.*;
import simulation.*;
import agents.agentokolia.continualassistants.*;

//meta! id="3"
public class AgentOkolia extends OSPABA.Agent
{
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
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerOkolia(Id.managerOkolia, mySim(), this);
		new ZachrankaScheduler(Id.zachrankaScheduler, mySim(), this);
		new PesoScheduler(Id.pesoScheduler, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.odchodPacienta);
	}
	//meta! tag="end"
}
