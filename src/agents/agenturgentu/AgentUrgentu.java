package agents.agenturgentu;

import OSPABA.*;
import simulation.*;

//meta! id="14"
public class AgentUrgentu extends OSPABA.Agent
{
	public AgentUrgentu(int id, Simulation mySim, Agent parent)
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
		new ManagerUrgentu(Id.managerUrgentu, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.osetreniePacienta);
		addOwnMessage(Mc.pridelenieSestry);
		addOwnMessage(Mc.pridelenieAmbulancie);
		addOwnMessage(Mc.pridelenieLekara);
		addOwnMessage(Mc.obsluhaPacienta);
		addOwnMessage(Mc.vstupneVysetreniePacienta);
	}
	//meta! tag="end"
}
