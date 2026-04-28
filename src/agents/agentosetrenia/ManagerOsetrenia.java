package agents.agentosetrenia;

import OSPABA.*;
import simulation.*;

//meta! id="24"
public class ManagerOsetrenia extends OSPABA.Manager
{
	public ManagerOsetrenia(int id, Simulation mySim, Agent myAgent)
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
	}

	//meta! sender="AgentUrgentu", id="26", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="27", type="Request"
	public void processOsetreniePacienta(MessageForm message)
	{
        message.setAddressee(Id.presunDoAmbulancie);
        startContinualAssistant(message);
	}

	//meta! sender="PresunDoAmbulancie", id="79", type="Finish"
	public void processFinishPresunDoAmbulancie(MessageForm message)
	{
        message.setAddressee(Id.vykonanieOsetrenia);
        startContinualAssistant(message);
	}

	//meta! sender="VykonanieOsetrenia", id="69", type="Finish"
	public void processFinishVykonanieOsetrenia(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        uvolniZdrojePredOdchodom(msg);

        message.setAddressee(Id.odchodPacienta);
        startContinualAssistant(message);
	}

	//meta! sender="OdchodPacienta", id="81", type="Finish"
	public void processFinishOdchodPacienta(MessageForm message)
	{
        message.setCode(Mc.osetreniePacienta);
        response(message);
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
		case Mc.finish:
			switch (message.sender().id())
			{
			case Id.presunDoAmbulancie:
				processFinishPresunDoAmbulancie(message);
			break;

			case Id.vykonanieOsetrenia:
				processFinishVykonanieOsetrenia(message);
			break;

			case Id.odchodPacienta:
				processFinishOdchodPacienta(message);
			break;
			}
		break;

		case Mc.osetreniePacienta:
			processOsetreniePacienta(message);
		break;

		case Mc.init:
			processInit(message);
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

    private void uvolniZdrojePredOdchodom(MyMessage msg)
    {
        if (msg.getSestra() != null) {
            MyMessage uvolnenie = new MyMessage(mySim());
            uvolnenie.setCode(Mc.uvolnenieSestry);
            uvolnenie.setAddressee(Id.agentSestier);
            uvolnenie.setSestra(msg.getSestra());
            notice(uvolnenie);

            msg.getPacient().setAktualnaSestra(null);
            msg.setSestra(null);
        }

        if (msg.getLekar() != null) {
            MyMessage uvolnenie = new MyMessage(mySim());
            uvolnenie.setCode(Mc.uvolnenieLekara);
            uvolnenie.setAddressee(Id.agentLekarov);
            uvolnenie.setLekar(msg.getLekar());
            notice(uvolnenie);

            msg.getPacient().setAktualnyLekar(null);
            msg.setLekar(null);
        }

        if (msg.getAmbulancia() != null) {
            MyMessage uvolnenie = new MyMessage(mySim());
            uvolnenie.setCode(Mc.uvolnenieAmbulancie);
            uvolnenie.setAddressee(Id.agentAmbulancii);
            uvolnenie.setAmbulancia(msg.getAmbulancia());
            notice(uvolnenie);

            msg.getPacient().setAktualnaAmbulancia(null);
            msg.setAmbulancia(null);
        }
    }

}
