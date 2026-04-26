package agents.agentsestier;

import OSPABA.*;
import simulation.*;

//meta! id="35"
public class ManagerSestier extends OSPABA.Manager
{
	public ManagerSestier(int id, Simulation mySim, Agent myAgent)
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

	//meta! sender="AgentUrgentu", id="39", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="44", type="Request"
	public void processPridelenieSestry(MessageForm message)
	{
        message.setAddressee(Id.vyberSestru);
        execute(message);
        response(message);
	}

	//meta! sender="AgentUrgentu", id="43", type="Notice"
	public void processUvolnenieSestry(MessageForm message)
	{
        message.setAddressee(Id.uvolniSestru);
        execute(message);
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
		case Mc.pridelenieSestry:
			processPridelenieSestry(message);
		break;

		case Mc.init:
			processInit(message);
		break;

		case Mc.uvolnenieSestry:
			processUvolnenieSestry(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentSestier myAgent()
	{
		return (AgentSestier)super.myAgent();
	}

}
