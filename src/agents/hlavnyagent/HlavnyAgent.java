package agents.hlavnyagent;

import OSPABA.*;
import simulation.*;

//meta! id="1"
public class HlavnyAgent extends OSPABA.Agent
{
	public HlavnyAgent(int id, Simulation mySim, Agent parent)
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
		new HlavnyManager(Id.hlavnyManager, mySim(), this);
		addOwnMessage(Mc.obsluhaPacienta);
		addOwnMessage(Mc.prichodPacienta);
	}
	//meta! tag="end"
}
