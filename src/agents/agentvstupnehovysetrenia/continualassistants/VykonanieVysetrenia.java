package agents.agentvstupnehovysetrenia.continualassistants;

import OSPABA.*;
import entity.Pacient;
import generatory.ContinousEmpiricGenerator;
import generatory.DiscreteEmpiricGenerator;
import generatory.DiscreteGenerator;
import generatory.EmpiricData;
import simulation.*;
import agents.agentvstupnehovysetrenia.*;
import OSPABA.Process;

import java.util.ArrayList;

//meta! id="62"
public class VykonanieVysetrenia extends OSPABA.Process
{
    ContinousEmpiricGenerator empiricGenerator;
    DiscreteGenerator discreteGenerator;

	public VykonanieVysetrenia(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        MySimulation mySim = (MySimulation)super.mySim();

        this.discreteGenerator = new DiscreteGenerator(4, 8, mySim.getSeedGenerator());

        ArrayList<EmpiricData> empiricDataList = new ArrayList<>();
        empiricDataList.add(new EmpiricData(3 * 60,5 * 60,0.6));
        empiricDataList.add(new EmpiricData(5 * 60,9 * 60,0.4));
        this.empiricGenerator = new ContinousEmpiricGenerator(mySim.getSeedGenerator(), empiricDataList);
	}

	//meta! sender="AgentVstupnehoVysetrenia", id="63", type="Start"
	public void processStart(MessageForm message)
	{
        if (message.lastPost() == MessageForm.PostType.start) {
            MyMessage msg = (MyMessage) message;
            double dur = vygenerujCasVysetrenia(msg);
            ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id()
                    + " vstupné vyšetrenie: " + String.format("%.0f", dur) + "s");
            hold(dur, message);
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
	public AgentVstupnehoVysetrenia myAgent()
	{
		return (AgentVstupnehoVysetrenia)super.myAgent();
	}

    private double vygenerujCasVysetrenia(MyMessage msg) {
        Pacient pacient = msg.getPacient();

        double cas;

        if (pacient.getTyp() == Pacient.TypPacienta.PESO) {
            cas = empiricGenerator.nextDouble();
        } else if (pacient.getTyp() == Pacient.TypPacienta.SANITKA) {
            cas = discreteGenerator.nextInt() * 60;
        } else {
            throw new IllegalStateException("Neznamy typ pacienta: " + pacient.getTyp());
        }

        return cas;
    }

}
