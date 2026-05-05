package agents.agenturgentu.continualassistants;

import OSPABA.*;
import simulation.*;
import agents.agenturgentu.*;

//meta! id="150"
public class PeciatkaZahrievania extends OSPABA.Scheduler
{
	public PeciatkaZahrievania(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentUrgentu", id="151", type="Start"
	public void processStart(MessageForm message)
	{
        if (message.lastPost() == MessageForm.PostType.start) {
            hold(MySimulation.SNAPSHOT_INTERVAL, message);
        } else {
            assistantFinished(message);
        }
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
	public AgentUrgentu myAgent()
	{
		return (AgentUrgentu)super.myAgent();
	}

}