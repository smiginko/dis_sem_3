package agents.agentlekarov;

import OSPABA.*;
import simulation.*;
import agents.agentlekarov.instantassistants.*;

//meta! id="45"
public class AgentLekarov extends OSPABA.Agent
{
	public AgentLekarov(int id, Simulation mySim, Agent parent)
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
		new ManagerLekarov(Id.managerLekarov, mySim(), this);
		new VyberLekara(Id.vyberLekara, mySim(), this);
		new UvolniLekara(Id.uvolniLekara, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.pridelenieLekara);
		addOwnMessage(Mc.uvolnenieLekara);
	}
	//meta! tag="end"
}
