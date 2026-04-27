package agents.agenturgentu.continualassistants;

import OSPABA.*;
import entity.Pacient;
import generatory.ContinousGenerator;
import generatory.TriangularDistribution;
import simulation.*;
import agents.agenturgentu.*;
import OSPABA.Process;

//meta! id="107"
public class PresunDoCakarne extends OSPABA.Process
{
    private TriangularDistribution pesoGenerator;
    private ContinousGenerator sanitkaGenerator;

    public PresunDoCakarne(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        MySimulation sim = (MySimulation) mySim();

        this.pesoGenerator = new TriangularDistribution(sim.getSeedGenerator(),120,300,150);

        this.sanitkaGenerator = new ContinousGenerator(90,200, sim.getSeedGenerator());
	}

	//meta! sender="AgentUrgentu", id="108", type="Start"
	public void processStart(MessageForm message)
	{
        if (message.lastPost() == MessageForm.PostType.start) {
            hold(vygenerujCasPresunu((MyMessage) message), message);
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

    private double vygenerujCasPresunu(MyMessage msg)
    {
        if (msg.getPacient().getTyp() == Pacient.TypPacienta.PESO) {
            return pesoGenerator.nextValue();
        }

        if (msg.getPacient().getTyp() == Pacient.TypPacienta.SANITKA) {
            return sanitkaGenerator.nextDouble();
        }

        throw new IllegalStateException("Neznamy typ pacienta");
    }

}