package agents.agentsestier;

import OSPABA.*;
import entity.Sestra;
import simulation.*;

import java.util.PriorityQueue;

//meta! id="35"
public class ManagerSestier extends OSPABA.Manager
{
    PriorityQueue<MyMessage> radCakajucich;

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

        this.radCakajucich = new PriorityQueue<>(MyMessage.PORADIE_OSETRENIE);
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
            response(msg);
        } else  {
            radCakajucich.offer(msg);
        }
	}

	//meta! sender="AgentUrgentu", id="43", type="Notice"
	public void processUvolnenieSestry(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        myAgent().uvolniSestru(msg.getSestra());

        skusPridatSestruDalsiemu();
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
		case Mc.uvolnenieSestry:
			processUvolnenieSestry(message);
		break;

		case Mc.init:
			processInit(message);
		break;

		case Mc.pridelenieSestry:
			processPridelenieSestry(message);
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
                response(cakajucaSprava);
            }
        }
    }

}