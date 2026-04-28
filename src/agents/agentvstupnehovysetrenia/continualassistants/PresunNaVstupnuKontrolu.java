package agents.agentvstupnehovysetrenia.continualassistants;

import OSPABA.*;
import generatory.TriangularDistribution;
import simulation.*;
import agents.agentvstupnehovysetrenia.*;
import OSPABA.Process;

//meta! id="71"
public class PresunNaVstupnuKontrolu extends OSPABA.Process
{
    private TriangularDistribution presunGenerator;

	public PresunNaVstupnuKontrolu(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        MySimulation sim = (MySimulation) mySim();
        this.presunGenerator = new TriangularDistribution( sim.getSeedGenerator(),15,45,20 );
	}

	//meta! sender="AgentVstupnehoVysetrenia", id="72", type="Start"
	public void processStart(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        if (message.lastPost() == MessageForm.PostType.start) {
            double casPresunu = vypocitajCasPresunuSestry(msg);

            if (casPresunu <= 0.0) {
                msg.getSestra().setPoloha(msg.getAmbulancia());
                assistantFinished(message);
                return;
            }

            hold(casPresunu, message);
            return;
        }

        if (message.lastPost() == MessageForm.PostType.hold) {
            msg.getSestra().setPoloha(msg.getAmbulancia());
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

    private double vypocitajCasPresunuSestry(MyMessage msg)
    {
        if (msg.getSestra() == null) {
            throw new IllegalStateException("PresunNaVstupnuKontrolu: chyba sestra");
        }

        if (msg.getAmbulancia() == null) {
            throw new IllegalStateException("PresunNaVstupnuKontrolu: chyba ambulancia");
        }

        if (msg.getSestra().getPoloha() == msg.getAmbulancia()) {
            return 0.0;
        }

        return presunGenerator.nextValue();
    }

}
