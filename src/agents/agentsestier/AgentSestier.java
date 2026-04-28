package agents.agentsestier;

import OSPABA.*;
import simulation.*;
import entity.Sestra;
import java.util.ArrayList;
import java.util.List;

//meta! id="35"
public class AgentSestier extends OSPABA.Agent
{

    private final List<Sestra> sestry = new ArrayList<>();

	public AgentSestier(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        sestry.clear();

        MySimulation sim = (MySimulation) mySim();

        for (int i = 0; i < sim.getPocetSestier(); i++) {
            sestry.add(new Sestra(mySim()));
        }
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerSestier(Id.managerSestier, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.pridelenieSestry);
		addOwnMessage(Mc.uvolnenieSestry);
	}
	//meta! tag="end"

    public Sestra vyberVolnuSestru()
    {
        StrategiaPridelovania strategia =
                ((MySimulation) mySim()).getStrategiaPridelovania();

        switch (strategia) {
            case PRVA_VOLNA:
                return vyberPrvuVolnuSestru();

            case NAJDLHSIE_VOLNA:
                return null;

            default:
                return vyberPrvuVolnuSestru();
        }
    }

    private Sestra vyberPrvuVolnuSestru()
    {
        for (Sestra sestra : sestry) {
            if (!sestra.jeObsadena()) {
                sestra.setJeObsadena(true);
                return sestra;
            }
        }

        return null;
    }

    public void uvolniSestru(Sestra sestra)
    {
        if (sestra != null) {
            sestra.setJeObsadena(false);
            sestra.setAktualnyPacient(null);
            if (sestra.getAktualnaAmbulancia() != null) {
                sestra.setPoloha(sestra.getAktualnaAmbulancia());
            }
            sestra.setAktualnaAmbulancia(null);
        }
    }
}