package agents.hlavnyagent;

import OSPABA.*;
import simulation.*;

//meta! id="1"
public class HlavnyManager extends OSPABA.Manager
{
	public HlavnyManager(int id, Simulation mySim, Agent myAgent)
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
        double warmupTime = ((MySimulation) mySim()).getWarmupTime();

        if (warmupTime > 0) {
            MyMessage msg = new MyMessage(mySim());
            msg.setAddressee(Id.zahrievanie);
            startContinualAssistant(msg);
        }
	}

	//meta! sender="AgentUrgentu", id="18", type="Response"
	public void processObsluhaPacienta(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        MyMessage odchod = new MyMessage(msg);
        odchod.setCode(Mc.odchodPacienta);
        odchod.setAddressee(Id.agentOkolia);
        notice(odchod);
	}

	//meta! sender="AgentOkolia", id="12", type="Notice"
	public void processPrichodPacienta(MessageForm message)
	{
        message.setCode(Mc.obsluhaPacienta);
        message.setAddressee(Id.agentUrgentu);
        request(message);
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! sender="Zahrievanie", id="136", type="Finish"
	public void processFinish(MessageForm message)
	{
        if (message.sender().id() == Id.zahrievanie) {
            MyMessage u = new MyMessage(mySim());
            u.setCode(Mc.koniecZahrievania);
            u.setAddressee(Id.agentUrgentu);
            notice(u);

            MyMessage o = new MyMessage(mySim());
            o.setCode(Mc.koniecZahrievania);
            o.setAddressee(Id.agentOkolia);
            notice(o);

            MySimulation s = (MySimulation) mySim();
            s.signalWarmupEnded();
            Double desired = s.getDesiredSpeedDuration();
            if (desired != null) {
                s.setSimSpeed(1.0, desired);
            }
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
		case Mc.prichodPacienta:
			processPrichodPacienta(message);
		break;

		case Mc.finish:
			processFinish(message);
		break;

		case Mc.obsluhaPacienta:
			processObsluhaPacienta(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public HlavnyAgent myAgent()
	{
		return (HlavnyAgent)super.myAgent();
	}

}