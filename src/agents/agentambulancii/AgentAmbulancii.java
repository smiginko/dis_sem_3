package agents.agentambulancii;

import OSPABA.*;
import simulation.*;
import entity.Ambulancia;
import java.util.ArrayList;
import java.util.List;

//meta! id="29"
public class AgentAmbulancii extends OSPABA.Agent
{

    private final List<Ambulancia> ambulancie = new ArrayList<>();

	public AgentAmbulancii(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

    public List<MyMessage> getRadCakajucich() {
        return ((ManagerAmbulancii) myManager()).getRadCakajucich();
    }

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
        ambulancie.clear();

        MySimulation sim = (MySimulation) mySim();

        int layoutIndex = 0;

        for (int i = 0; i < sim.getPocetAmbulanciiA(); i++) {
            ambulancie.add(new Ambulancia(mySim(), Ambulancia.TypeAmbulancia.TYP_A, layoutIndex++));
        }

        for (int i = 0; i < sim.getPocetAmbulanciiB(); i++) {
            ambulancie.add(new Ambulancia(mySim(), Ambulancia.TypeAmbulancia.TYP_B, layoutIndex++));
        }
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerAmbulancii(Id.managerAmbulancii, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.pridelenieAmbulancie);
		addOwnMessage(Mc.uvolnenieAmbulancie);
	}
	//meta! tag="end"

    public Ambulancia vyberVolnuAmbulanciu(boolean povolenaA, boolean povolenaB)
    {
        StrategiaPridelovania strategia =
                ((MySimulation) mySim()).getStrategiaPridelovania();

        switch (strategia) {
            case PRVA_VOLNA:
                return vyberPrvuVolnuAmbulanciu(povolenaA, povolenaB);

            case NAJDLHSIE_VOLNA:
                return null;


            default:
                return vyberPrvuVolnuAmbulanciu(povolenaA, povolenaB);
        }
    }

    private Ambulancia vyberPrvuVolnuAmbulanciu(boolean povolenaA, boolean povolenaB)
    {
        for (Ambulancia ambulancia : ambulancie) {
            if (ambulancia.isJeObsadena()) {
                continue;
            }

            boolean jeA = ambulancia.getTyp() == Ambulancia.TypeAmbulancia.TYP_A;
            boolean jeB = ambulancia.getTyp() == Ambulancia.TypeAmbulancia.TYP_B;

            if ((povolenaA && jeA) || (povolenaB && jeB)) {
                ambulancia.setJeObsadena(true);
                return ambulancia;
            }
        }

        return null;
    }

    public void uvolniAmbulanciu(Ambulancia ambulancia)
    {
        if (ambulancia != null) {
            ambulancia.setJeObsadena(false);
        }
    }

    public List<Ambulancia> getAmbulancies() {
        return ambulancie;
    }
}