package agents.agentlekarov;

import OSPABA.*;
import entity.Lekar;
import simulation.*;
import statistiky.TimeWeightedStatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

//meta! id="45"
public class ManagerLekarov extends OSPABA.Manager
{
    PriorityQueue<MyMessage> radCakajucich;

    TimeWeightedStatistic vytazenieLekarovStat;

    public List<MyMessage> getRadCakajucich() {
        return new ArrayList<>(radCakajucich);
    }

	public ManagerLekarov(int id, Simulation mySim, Agent myAgent)
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

        vytazenieLekarovStat = new TimeWeightedStatistic("Vytazenie lekarov",
                mySim().currentTime(), 0);
	}

	//meta! sender="AgentUrgentu", id="49", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="51", type="Request"
	public void processPridelenieLekara(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        Lekar volnyLekar = myAgent().vyberVolnehoLekara();

        if (volnyLekar != null) {
            msg.setLekar(volnyLekar);
            vytazenieLekarovStat.update((double) myAgent().getPocetObsadenychLekarov() / ((MySimulation) mySim()).getPocetLekarov(),
                    mySim().currentTime());
            response(msg);
        } else {
            ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id()
                    + " čaká na lekára");
            radCakajucich.offer(msg);
        }
	}

	//meta! sender="AgentUrgentu", id="50", type="Notice"
	public void processUvolnenieLekara(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        myAgent().uvolniLekara(msg.getLekar());

        vytazenieLekarovStat.update((double) myAgent().getPocetObsadenychLekarov() / ((MySimulation) mySim()).getPocetLekarov(),
                mySim().currentTime());

        skusPridatLekaraDalsiemu();
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
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
		case Mc.uvolnenieLekara:
			processUvolnenieLekara(message);
		break;

		case Mc.init:
			processInit(message);
		break;

		case Mc.pridelenieLekara:
			processPridelenieLekara(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentLekarov myAgent()
	{
		return (AgentLekarov)super.myAgent();
	}

    private void skusPridatLekaraDalsiemu() {
        if (!radCakajucich.isEmpty()) {
            Lekar volnyLekar = myAgent().vyberVolnehoLekara();

            if (volnyLekar != null) {
                MyMessage cakajucaSprava = radCakajucich.poll();
                cakajucaSprava.setLekar(volnyLekar);
                vytazenieLekarovStat.update((double) myAgent().getPocetObsadenychLekarov() / ((MySimulation) mySim()).getPocetLekarov(),
                        mySim().currentTime());
                response(cakajucaSprava);
            }
        }
    }

    public TimeWeightedStatistic getVytazenieLekarovStat() {
        return vytazenieLekarovStat;
    }
}