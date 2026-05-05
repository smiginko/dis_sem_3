package agents.agentosetrenia.continualassistants;

import OSPABA.*;
import agents.agentosetrenia.*;
import generatory.ExponentialDistribution;
import generatory.TriangularDistribution;
import simulation.*;
import OSPABA.Process;

//meta! id="78"
public class PresunDoAmbulancie extends OSPABA.Process
{
    private TriangularDistribution generator;

	public PresunDoAmbulancie(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        MySimulation mySimulation = (MySimulation) mySim();
        this.generator = new TriangularDistribution(mySimulation.getSeedGenerator(), 15,45,20);
	}

	//meta! sender="AgentOsetrenia", id="79", type="Start"
	public void processStart(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;
        MySimulation s = (MySimulation) mySim();

        if (message.lastPost() == MessageForm.PostType.start) {
            double casLekar = msg.getAmbulancia().equals(msg.getLekar().getPoloha())
                    ? 0 : generator.nextValue();
            double casSestra = msg.getAmbulancia().equals(msg.getSestra().getPoloha())
                    ? 0 : generator.nextValue();
            double casPacient = msg.getAmbulancia().equals(msg.getPacient().getPoloha())
                    ? 0 : generator.nextValue();

            double bigger = Math.max(casSestra, casLekar);
            double delay = Math.max(bigger, casPacient);

            s.animaciaUrgentu().presunSestruDoAmbulancie(msg.getSestra(), msg.getAmbulancia(), casSestra);
            s.animaciaUrgentu().presunLekaraDoAmbulancie(msg.getLekar(), msg.getAmbulancia(), casLekar);
            s.animaciaUrgentu().presunPacientaDoAmbulancie(msg.getPacient(), msg.getAmbulancia(), casPacient);

            if (delay > 0) {
                s.log("Presun ku ambulancii " + msg.getAmbulancia().id()
                        + " (lekár=" + String.format("%.0f", casLekar) + "s"
                        + ", sestra=" + String.format("%.0f", casSestra) + "s"
                        + ", pacient=" + String.format("%.0f", casPacient) + "s)");
                hold(delay, message);
            } else {
                s.log("Personál a pacient už pri ambulancii " + msg.getAmbulancia().id());
                msg.getSestra().setPoloha(msg.getAmbulancia());
                msg.getLekar().setPoloha(msg.getAmbulancia());
                msg.getPacient().setPoloha(msg.getAmbulancia());
                assistantFinished(message);
            }
            return;
        }

        if (message.lastPost() == MessageForm.PostType.hold) {
            msg.getSestra().setPoloha(msg.getAmbulancia());
            msg.getLekar().setPoloha(msg.getAmbulancia());
            msg.getPacient().setPoloha(msg.getAmbulancia());
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
