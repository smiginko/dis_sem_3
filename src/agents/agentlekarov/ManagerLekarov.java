package agents.agentlekarov;

import OSPABA.*;
import simulation.*;

//meta! id="45"
public class ManagerLekarov extends OSPABA.Manager
{
	public ManagerLekarov(int id, Simulation mySim, Agent myAgent)
	{
		super(id, mySim, myAgent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication

		if (petriNet() != null)
		{
			petriNet().clear();
		}
	}

	//meta! sender="AgentUrgentu", id="49", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="51", type="Request"
	public void processPridelenieLekara(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="50", type="Notice"
	public void processUvolnenieLekara(MessageForm message)
	{
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	public void init()
	{
	}

	@Override
	public void processMessage(MessageForm message)
	{
		switch (message.code())
		{
		case Mc.pridelenieLekara:
			processPridelenieLekara(message);
		break;

		case Mc.uvolnenieLekara:
			processUvolnenieLekara(message);
		break;

		case Mc.init:
			processInit(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentLekarov myAgent()
	{
		return (AgentLekarov)super.myAgent();
	}

}
