package agents.agentambulancii;

import OSPABA.*;
import simulation.*;

//meta! id="29"
public class ManagerAmbulancii extends OSPABA.Manager
{
	public ManagerAmbulancii(int id, Simulation mySim, Agent myAgent)
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

	//meta! sender="AgentUrgentu", id="31", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="32", type="Request"
	public void processPridelenieAmbulancie(MessageForm message)
	{
        message.setAddressee(Id.vyberAmbulanciu);
        execute(message);
        response(message);
	}

	//meta! sender="AgentUrgentu", id="34", type="Notice"
	public void processUvolnenieAmbulancie(MessageForm message)
	{
        message.setAddressee(Id.uvolniAmbulanciu);
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
		case Mc.uvolnenieAmbulancie:
			processUvolnenieAmbulancie(message);
		break;

		case Mc.pridelenieAmbulancie:
			processPridelenieAmbulancie(message);
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
	public AgentAmbulancii myAgent()
	{
		return (AgentAmbulancii)super.myAgent();
	}

}
