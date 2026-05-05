package agents.agentambulancii;

import OSPABA.*;
import entity.Ambulancia;
import simulation.*;
import statistiky.TimeWeightedStatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

//meta! id="29"
public class ManagerAmbulancii extends OSPABA.Manager
{
    PriorityQueue<MyMessage> radCakajucich;

    private TimeWeightedStatistic vytazenieAmbulanciiA;
    private TimeWeightedStatistic vytazenieAmbulanciiB;

    public List<MyMessage> getRadCakajucich() {
        return new ArrayList<>(radCakajucich);
    }

	public ManagerAmbulancii(int id, Simulation mySim, Agent myAgent)
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

        this.vytazenieAmbulanciiA = new TimeWeightedStatistic(
                "Vytazenie ambulancii typ A",
                mySim().currentTime(),
                0
        );

        this.vytazenieAmbulanciiB = new TimeWeightedStatistic(
                "Vytazenie ambulancii typ B",
                mySim().currentTime(),
                0
        );
	}

	//meta! sender="AgentUrgentu", id="31", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="32", type="Request"
	public void processPridelenieAmbulancie(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        Ambulancia volnaAmbulancia = myAgent().vyberVolnuAmbulanciu(msg.isPovolenaAmbulanciaA(), msg.isPovolenaAmbulanciaB());

        if (volnaAmbulancia != null) {
            msg.setAmbulancia(volnaAmbulancia);
            aktualizujVytazenieAmbulancii();
            response(msg);
        } else {
            ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id()
                    + " čaká na ambulanciu (A=" + msg.isPovolenaAmbulanciaA()
                    + ", B=" + msg.isPovolenaAmbulanciaB() + ")");
            radCakajucich.offer(msg);
        }
	}

	//meta! sender="AgentUrgentu", id="34", type="Notice"
	public void processUvolnenieAmbulancie(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        myAgent().uvolniAmbulanciu(msg.getAmbulancia());

        aktualizujVytazenieAmbulancii();

        skusPridatAmbulanciuDalsiemu();
	}

	//meta! sender="AgentUrgentu", id="148", type="Notice"
	public void processKoniecZahrievania(MessageForm message)
    {
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
		case Mc.init:
			processInit(message);
		break;

		case Mc.pridelenieAmbulancie:
			processPridelenieAmbulancie(message);
		break;

		case Mc.uvolnenieAmbulancie:
			processUvolnenieAmbulancie(message);
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
	public AgentAmbulancii myAgent()
	{
		return (AgentAmbulancii)super.myAgent();
	}

    private void skusPridatAmbulanciuDalsiemu() {
        for (MyMessage msg : radCakajucich) {
            Ambulancia a = myAgent().vyberVolnuAmbulanciu(msg.isPovolenaAmbulanciaA(), msg.isPovolenaAmbulanciaB());
            if (a != null) {
                radCakajucich.remove(msg);
                msg.setAmbulancia(a);
                aktualizujVytazenieAmbulancii();
                response(msg);
                break;
            }
        }
    }

    public TimeWeightedStatistic getVytazenieAmbulanciiA() {
        return vytazenieAmbulanciiA;
    }

    public TimeWeightedStatistic getVytazenieAmbulanciiB() {
        return vytazenieAmbulanciiB;
    }

    private void aktualizujVytazenieAmbulancii() {
        MySimulation sim = (MySimulation) mySim();

        double vytazenieA = 0.0;
        if (sim.getPocetAmbulanciiA() > 0) {
            vytazenieA = (double) myAgent().getPocetObsadenychAmbulanciiA()
                    / sim.getPocetAmbulanciiA();
        }

        double vytazenieB = 0.0;
        if (sim.getPocetAmbulanciiB() > 0) {
            vytazenieB = (double) myAgent().getPocetObsadenychAmbulanciiB()
                    / sim.getPocetAmbulanciiB();
        }

        vytazenieAmbulanciiA.update(vytazenieA, mySim().currentTime());
        vytazenieAmbulanciiB.update(vytazenieB, mySim().currentTime());
    }
}