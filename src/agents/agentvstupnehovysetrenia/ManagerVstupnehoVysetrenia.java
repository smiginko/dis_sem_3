package agents.agentvstupnehovysetrenia;

import OSPABA.*;
import simulation.*;

//meta! id="19"
public class ManagerVstupnehoVysetrenia extends OSPABA.Manager
{
	public ManagerVstupnehoVysetrenia(int id, Simulation mySim, Agent myAgent)
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

	//meta! sender="AgentUrgentu", id="23", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="PresunNaVstupnuKontrolu", id="72", type="Finish"
	public void processFinishPresunNaVstupnuKontrolu(MessageForm message)
	{
        message.setAddressee(Id.vykonanieVysetrenia);
        startContinualAssistant(message);
	}

	//meta! sender="VykonanieVysetrenia", id="63", type="Finish"
	public void processFinishVykonanieVysetrenia(MessageForm message)
	{
        message.setAddressee(Id.prideleniePriority);
        execute(message);

        message.setCode(Mc.vstupneVysetreniePacienta);
        response(message);
	}

	//meta! sender="AgentUrgentu", id="22", type="Request"
	public void processVstupneVysetreniePacienta(MessageForm message)
	{
        message.setAddressee(Id.presunNaVstupnuKontrolu);
        startContinualAssistant(message);
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
		case Mc.finish:
			switch (message.sender().id())
			{
			case Id.presunNaVstupnuKontrolu:
				processFinishPresunNaVstupnuKontrolu(message);
			break;

			case Id.vykonanieVysetrenia:
				processFinishVykonanieVysetrenia(message);
			break;
			}
		break;

		case Mc.vstupneVysetreniePacienta:
			processVstupneVysetreniePacienta(message);
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
	public AgentVstupnehoVysetrenia myAgent()
	{
		return (AgentVstupnehoVysetrenia)super.myAgent();
	}

}
