package agents.agentosetrenia.continualassistants;

import OSPABA.*;
import agents.agentosetrenia.*;
import entity.Pacient;
import generatory.ContinousEmpiricGenerator;
import generatory.ContinousGenerator;
import generatory.DiscreteEmpiricGenerator;
import generatory.EmpiricData;
import simulation.*;
import OSPABA.Process;

import java.util.ArrayList;

//meta! id="68"
public class VykonanieOsetrenia extends OSPABA.Process
{
    ContinousEmpiricGenerator empiricGenerator;
    ContinousGenerator continousGenerator;

	public VykonanieOsetrenia(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        MySimulation mySimulation = (MySimulation) mySim();
        this.continousGenerator = new ContinousGenerator(15 * 60,30 * 60, mySimulation.getSeedGenerator());

        ArrayList<EmpiricData> empiricDataList = new ArrayList<>();
        empiricDataList.add(new EmpiricData(10 * 60,12 * 60,0.1));
        empiricDataList.add(new EmpiricData(12 * 60,14 * 60,0.6));
        empiricDataList.add(new EmpiricData(14 * 60,18 * 60,0.3));
        this.empiricGenerator = new ContinousEmpiricGenerator(mySimulation.getSeedGenerator(), empiricDataList);
	}

	//meta! sender="AgentOsetrenia", id="69", type="Start"
	public void processStart(MessageForm message)
	{
        if (message.lastPost() == MessageForm.PostType.start) {
            hold(vygenerujCasOsetrenia((MyMessage) message), message);
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

    private double vygenerujCasOsetrenia(MyMessage msg) {
        Pacient pacient = msg.getPacient();

        double cas;

        if (pacient.getTyp() == Pacient.TypPacienta.PESO) {
            cas = empiricGenerator.nextDouble();
        } else if (pacient.getTyp() == Pacient.TypPacienta.SANITKA) {
            cas = continousGenerator.nextDouble();
        } else {
            throw new IllegalStateException("Neznamy typ pacienta: " + pacient.getTyp());
        }

        return cas;
    }

}
