package agents.agentsestier;

import OSPABA.*;
import entity.Sestra;
import simulation.*;
import statistiky.TimeWeightedStatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

//meta! id="35"
public class ManagerSestier extends OSPABA.Manager
{
    PriorityQueue<MyMessage> radCakajucich;

    TimeWeightedStatistic vytazenieSestryStat;

    public List<MyMessage> getRadCakajucich() {
        return new ArrayList<>(radCakajucich);
    }

	public ManagerSestier(int id, Simulation mySim, Agent myAgent)
	{
		super(id, mySim, myAgent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication

		if (petriNet() != null)
		{
			petriNet().clear();
		}

        this.radCakajucich = new PriorityQueue<>(MyMessage.PORADIE);

        vytazenieSestryStat = new TimeWeightedStatistic("Vytazenie sestier",
                mySim().currentTime(), 0);
	}

	//meta! sender="AgentUrgentu", id="39", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="44", type="Request"
	public void processPridelenieSestry(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        Sestra volnaSestra = myAgent().vyberVolnuSestru();

        if (volnaSestra != null) {
            msg.setSestra(volnaSestra);
            vytazenieSestryStat.update((double) myAgent().getPocetObsadenychSestier() / ((MySimulation) mySim()).getPocetSestier(),
                    mySim().currentTime());
            response(msg);
        } else {
            ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id()
                    + " čaká na sestru");
            radCakajucich.offer(msg);
        }
	}

	//meta! sender="AgentUrgentu", id="43", type="Notice"
	public void processUvolnenieSestry(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        myAgent().uvolniSestru(msg.getSestra());

        vytazenieSestryStat.update((double) myAgent().getPocetObsadenychSestier() / ((MySimulation) mySim()).getPocetSestier(),
                mySim().currentTime());

        skusPridatSestruDalsiemu();
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! sender="AgentUrgentu", id="146", type="Notice"
	public void processKoniecZahrievania(MessageForm message)
	{
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	public void init()
	{
	}

	@Override
	public void processMessage(MessageForm message)
	{
		switch (message.code())
		{
		case Mc.uvolnenieSestry:
			processUvolnenieSestry(message);
		break;

		case Mc.init:
			processInit(message);
		break;

		case Mc.pridelenieSestry:
			processPridelenieSestry(message);
		break;

		case Mc.koniecZahrievania:
			processKoniecZahrievania(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentSestier myAgent()
	{
		return (AgentSestier)super.myAgent();
	}

    private void skusPridatSestruDalsiemu() {
        if (!radCakajucich.isEmpty()) {
            Sestra volnaSestra = myAgent().vyberVolnuSestru();

            if (volnaSestra != null) {
                MyMessage cakajucaSprava = radCakajucich.poll();
                cakajucaSprava.setSestra(volnaSestra);
                vytazenieSestryStat.update((double) myAgent().getPocetObsadenychSestier() / ((MySimulation) mySim()).getPocetSestier(),
                        mySim().currentTime());
                response(cakajucaSprava);
            }
        }
    }

    public TimeWeightedStatistic getVytazenieSestryStat()
    {
        return vytazenieSestryStat;
    }
}