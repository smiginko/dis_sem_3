package agents.agentokolia;

import OSPABA.*;
import simulation.*;

//meta! id="3"
public class ManagerOkolia extends OSPABA.Manager
{
	public ManagerOkolia(int id, Simulation mySim, Agent myAgent)
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

	//meta! sender="HlavnyAgent", id="10", type="Notice"
	public void processInit(MessageForm message)
	{
        MyMessage pesoMessage = new MyMessage(mySim());
        pesoMessage.setAddressee(Id.pesoScheduler);
        startContinualAssistant(pesoMessage);

        MyMessage zachrankaMessage = new MyMessage(mySim());
        zachrankaMessage.setAddressee(Id.zachrankaScheduler);
        startContinualAssistant(zachrankaMessage);
	}

	//meta! sender="HlavnyAgent", id="13", type="Notice"
	public void processOdchodPacienta(MessageForm message)
	{
	}

	//meta! sender="ZachrankaScheduler", id="59", type="Finish"
	public void processFinishZachrankaScheduler(MessageForm message)
	{
        message.setCode(Mc.prichodPacienta);
        message.setAddressee(Id.hlavnyAgent);
        notice(message);
	}

	//meta! sender="PesoScheduler", id="6", type="Finish"
	public void processFinishPesoScheduler(MessageForm message)
	{
        message.setCode(Mc.prichodPacienta);
        message.setAddressee(Id.hlavnyAgent);
        notice(message);
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
		case Mc.init:
			processInit(message);
		break;

		case Mc.finish:
			switch (message.sender().id())
			{
			case Id.zachrankaScheduler:
				processFinishZachrankaScheduler(message);
			break;

			case Id.pesoScheduler:
				processFinishPesoScheduler(message);
			break;
			}
		break;

		case Mc.odchodPacienta:
			processOdchodPacienta(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentOkolia myAgent()
	{
		return (AgentOkolia)super.myAgent();
	}

}
