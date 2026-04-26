package agents.agentvstupnehovysetrenia.continualassistants;

import OSPABA.*;
import simulation.*;
import agents.agentvstupnehovysetrenia.*;
import OSPABA.Process;

//meta! id="71"
public class PresunNaVstupnuKontrolu extends OSPABA.Process
{
	public PresunNaVstupnuKontrolu(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentVstupnehoVysetrenia", id="72", type="Start"
	public void processStart(MessageForm message)
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
	@Override
	public void processMessage(MessageForm message)
	{
		switch (message.code())
		{
		case Mc.start:
			processStart(message);
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
