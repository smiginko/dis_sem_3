package agents.agentlekarov;

import OSPABA.*;
import entity.Lekar;
import entity.Sestra;
import simulation.*;
import agents.agentlekarov.instantassistants.*;

import java.util.ArrayList;
import java.util.List;

//meta! id="45"
public class AgentLekarov extends OSPABA.Agent
{

    private final List<Lekar> lekari = new ArrayList<>();

	public AgentLekarov(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        lekari.clear();

        MySimulation sim = (MySimulation) mySim();

        for (int i = 0; i < sim.getPocetLekarov(); i++) {
            lekari.add(new Lekar(mySim()));
        }
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerLekarov(Id.managerLekarov, mySim(), this);
		new VyberLekara(Id.vyberLekara, mySim(), this);
		new UvolniLekara(Id.uvolniLekara, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.pridelenieLekara);
		addOwnMessage(Mc.uvolnenieLekara);
	}
	//meta! tag="end"

    public Lekar vyberVolnehoLekara()
    {
        StrategiaPridelovania strategia =
                ((MySimulation) mySim()).getStrategiaPridelovania();

        switch (strategia) {
            case PRVA_VOLNA:
                return vyberPrvehoLekara();

            case NAJDLHSIE_VOLNA:
                return null;

            default:
                return vyberPrvehoLekara();
        }
    }

    private Lekar vyberPrvehoLekara()
    {
        for (Lekar lekar : lekari) {
            if (!lekar.jeObsadeny()) {
                lekar.setJeObsadeny(true);
                return lekar;
            }
        }
        return null;
    }

    public void uvolniLekara(Lekar lekar)
    {
        if (lekar != null) {
            lekar.setJeObsadeny(false);
            lekar.setAktualnyPacient(null);
            if (lekar.getAktualnaAmbulancia() != null) {
                lekar.setPoloha(lekar.getAktualnaAmbulancia());
            }
            lekar.setAktualnaAmbulancia(null);
        }
    }
}

