package agents.agenturgentu.continualassistants;

import OSPABA.*;
import generatory.ContinousGenerator;
import simulation.*;
import agents.agenturgentu.*;
import OSPABA.Process;

//meta! id="114"
public class PresunZCakarne extends OSPABA.Process
{
    private ContinousGenerator generator;

	public PresunZCakarne(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        MySimulation sim = (MySimulation) mySim();
        generator = new ContinousGenerator(150, 240, sim.getSeedGenerator());
	}

	//meta! sender="AgentUrgentu", id="115", type="Start"
	public void processStart(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        if (message.lastPost() == MessageForm.PostType.start) {
            double cas = generator.nextDouble();
            ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id()
                    + " odchod z urgentu: " + String.format("%.0f", cas) + "s");
            hold(cas, message);
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
	public AgentUrgentu myAgent()
	{
		return (AgentUrgentu)super.myAgent();
	}

}
