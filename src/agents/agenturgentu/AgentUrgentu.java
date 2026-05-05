package agents.agenturgentu;

import OSPABA.*;
import simulation.*;
import agents.agenturgentu.continualassistants.*;
import java.util.List;

//meta! id="14"
public class AgentUrgentu extends OSPABA.Agent
{
	public AgentUrgentu(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerUrgentu(Id.managerUrgentu, mySim(), this);
		new PresunDoCakarne(Id.presunDoCakarne, mySim(), this);
		new PresunZCakarne(Id.presunZCakarne, mySim(), this);
		new PeciatkaZahrievania(Id.peciatkaZahrievania, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.osetreniePacienta);
		addOwnMessage(Mc.pridelenieSestry);
		addOwnMessage(Mc.pridelenieAmbulancie);
		addOwnMessage(Mc.pridelenieLekara);
		addOwnMessage(Mc.koniecZahrievania);
		addOwnMessage(Mc.obsluhaPacienta);
		addOwnMessage(Mc.vstupneVysetreniePacienta);
	}
	//meta! tag="end"

    public List<MyMessage> getRadNaVstupneVysetrenie() {
        return ((ManagerUrgentu) myManager()).getRadNaVstupneVysetrenie();
    }

    public List<MyMessage> getRadNaOsetrenie() {
        return ((ManagerUrgentu) myManager()).getRadNaOsetrenie();
    }
}