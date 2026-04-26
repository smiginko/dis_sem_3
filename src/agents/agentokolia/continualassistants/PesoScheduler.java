package agents.agentokolia.continualassistants;

import OSPABA.*;
import agents.agentokolia.*;
import entity.Pacient;
import generatory.ExponentialDistribution;
import simulation.*;

//meta! id="5"
public class PesoScheduler extends OSPABA.Scheduler
{

    private ExponentialDistribution generator;

	public PesoScheduler(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        MySimulation mySimulation = (MySimulation) mySim();
        this.generator = new ExponentialDistribution(mySimulation.getSeedGenerator(), 129.14);
	}

	//meta! sender="AgentOkolia", id="6", type="Start"
	public void processStart(MessageForm message)
	{
        if (message.lastPost() == MessageForm.PostType.start) {
            naplanujDalsiPrichod();
            return;
        }

        if (message.lastPost() == MessageForm.PostType.hold) {
            MyMessage msg = (MyMessage) message;

            Pacient pacient = new Pacient(
                    mySim(),
                    Pacient.TypPacienta.PESO,
                    mySim().currentTime()
            );

            msg.setPacient(pacient);

            System.out.println("Vznikol PESO pacient id=" + pacient.id()
                    + " v case " + mySim().currentTime());

            assistantFinished(msg);

            naplanujDalsiPrichod();
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
	public AgentOkolia myAgent()
	{
		return (AgentOkolia)super.myAgent();
	}

    private void naplanujDalsiPrichod() {
        MyMessage dalsiPrichod = new MyMessage(mySim());
        dalsiPrichod.setCode(Mc.start);

        hold(generator.nextValue(), dalsiPrichod);
    }

}
