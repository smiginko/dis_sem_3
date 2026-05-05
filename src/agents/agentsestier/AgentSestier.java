package agents.agentsestier;

import OSPABA.*;
import entity.Ambulancia;
import simulation.*;
import entity.Sestra;
import statistiky.TimeWeightedStatistic;

import java.util.ArrayList;
import java.util.List;

//meta! id="35"
public class AgentSestier extends OSPABA.Agent
{
    private final List<Sestra> sestry = new ArrayList<>();

    TimeWeightedStatistic vytazenieSestryStat;

	public AgentSestier(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

    public List<MyMessage> getRadCakajucich() {
        return ((ManagerSestier) myManager()).getRadCakajucich();
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

        vytazenieSestryStat = new TimeWeightedStatistic("Vytazenie sestier",
                mySim().currentTime(), 0);
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerSestier(Id.managerSestier, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.pridelenieSestry);
		addOwnMessage(Mc.koniecZahrievania);
		addOwnMessage(Mc.uvolnenieSestry);
	}
	//meta! tag="end"

    public Sestra vyberVolnuSestru(Ambulancia cielovaAmbulancia)
    {
        StrategiaSestier strategia =
                ((MySimulation) mySim()).getStrategiaSestier();

        switch (strategia) {
            case PRVA_VOLNA_SESTRA:
                return vyberPrvuVolnuSestru();

            case SESTRA_V_AMBULANCII_INAK_PRVA_VOLNA:
                return vyberVolnuSestruMinimalnyPresun(cielovaAmbulancia);

            case SESTRA_V_AMBULANCII_INAK_NAJDLHSIE_VOLNA:
                return vyberVolnuSestruNajdlhsieVolna(cielovaAmbulancia);

            default:
                return vyberPrvuVolnuSestru();
        }
    }

    private Sestra vyberPrvuVolnuSestru()
    {
        for (Sestra sestra : sestry) {
            if (!sestra.jeObsadena()) {
                sestra.setJeObsadena(true);
                sestra.setCasOdKedyJeVolna(0.0);
                return sestra;
            }
        }

        return null;
    }

    public List<Sestra> getSestry() {
        return sestry;
    }

    public void uvolniSestru(Sestra sestra)
    {
        if (sestra != null) {
            sestra.setJeObsadena(false);
            sestra.setCasOdKedyJeVolna(mySim().currentTime());
            sestra.setAktualnyPacient(null);
            if (sestra.getAktualnaAmbulancia() != null) {
                sestra.setPoloha(sestra.getAktualnaAmbulancia());
            }
            sestra.setAktualnaAmbulancia(null);
        }
    }

    public int getPocetObsadenychSestier() {
        int pocet = 0;
        for (Sestra sestra : sestry) {
            if (sestra.jeObsadena()) {
                pocet++;
            }
        }
        return pocet;
    }

    private Sestra vyberVolnuSestruMinimalnyPresun(Ambulancia cielovaAmbulancia) {
        Sestra fallback = null;

        for (Sestra sestra : sestry) {
            if (sestra.jeObsadena()) {
                continue;
            }

            if (sestra.getPoloha() == cielovaAmbulancia) {
                sestra.setJeObsadena(true);
                sestra.setCasOdKedyJeVolna(0.0);
                return sestra;
            }

            if (fallback == null || sestra.id() < fallback.id()) {
                fallback = sestra;
            }
        }

        if (fallback != null) {
            fallback.setJeObsadena(true);
            fallback.setCasOdKedyJeVolna(0.0);
        }

        return fallback;
    }

    private Sestra vyberVolnuSestruNajdlhsieVolna(Ambulancia cielovaAmbulancia) {
        Sestra najdlhsieVolna = null;

        for (Sestra sestra : sestry) {
            if (sestra.jeObsadena()) {
                continue;
            }

            if (sestra.getPoloha() == cielovaAmbulancia) {
                sestra.setJeObsadena(true);
                sestra.setCasOdKedyJeVolna(0.0);
                return sestra;
            }

            if (najdlhsieVolna == null
                    || sestra.getCasOdKedyJeVolna() < najdlhsieVolna.getCasOdKedyJeVolna()
                    || (sestra.getCasOdKedyJeVolna() == najdlhsieVolna.getCasOdKedyJeVolna()
                    && sestra.id() < najdlhsieVolna.id())) {
                najdlhsieVolna = sestra;
            }
        }

        if (najdlhsieVolna != null) {
            najdlhsieVolna.setJeObsadena(true);
            najdlhsieVolna.setCasOdKedyJeVolna(0.0);
        }

        return najdlhsieVolna;
    }

    public void aktualizujVytazenieSestier() {
        MySimulation sim = (MySimulation) mySim();
        vytazenieSestryStat.update((double) getPocetObsadenychSestier() / sim.getPocetSestier(), mySim().currentTime());
    }

    public TimeWeightedStatistic getVytazenieSestryStat() {
        return vytazenieSestryStat;
    }

    public void resetStatistikyPoZahrievani() {
        MySimulation sim = (MySimulation) mySim();

        double vytazenie = 0.0;
        if (sim.getPocetSestier() > 0) {
            vytazenie = (double) getPocetObsadenychSestier() / sim.getPocetSestier();
        }

        vytazenieSestryStat.reset(mySim().currentTime(), vytazenie);
    }
}
