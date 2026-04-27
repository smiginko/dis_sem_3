package agents.agentosetrenia.continualassistants;

import OSPABA.*;
import agents.agentosetrenia.*;
import generatory.ContinousGenerator;
import generatory.DiscreteGenerator;
import simulation.*;
import OSPABA.Process;

//meta! id="80"
public class OdchodPacienta extends OSPABA.Process
{
    ContinousGenerator generator;

	public OdchodPacienta(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        MySimulation mySimulation = (MySimulation) mySim();
        this.generator = new ContinousGenerator(150, 240, mySimulation.getSeedGenerator());
	}

	//meta! sender="AgentOsetrenia", id="81", type="Start"
	public void processStart(MessageForm message)
	{
        if (message.lastPost() == MessageForm.PostType.start) {
            hold(this.generator.nextDouble(), message);
            return;
        }

        if (message.lastPost() == MessageForm.PostType.hold) {
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
	public AgentOsetrenia myAgent()
	{
		return (AgentOsetrenia)super.myAgent();
	}

}
