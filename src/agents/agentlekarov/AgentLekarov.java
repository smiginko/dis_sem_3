package agents.agentlekarov;

import OSPABA.*;
import entity.Ambulancia;
import simulation.*;
import entity.Lekar;
import statistiky.TimeWeightedStatistic;

import java.util.ArrayList;
import java.util.List;

//meta! id="45"
public class AgentLekarov extends OSPABA.Agent
{
    private final List<Lekar> lekari = new ArrayList<>();

    TimeWeightedStatistic vytazenieLekarovStat;

	public AgentLekarov(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

    public List<MyMessage> getRadCakajucich() {
        return ((ManagerLekarov) myManager()).getRadCakajucich();
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

        vytazenieLekarovStat = new TimeWeightedStatistic("Vytazenie lekarov",
                mySim().currentTime(), 0);
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerLekarov(Id.managerLekarov, mySim(), this);
		addOwnMessage(Mc.init);
		addOwnMessage(Mc.pridelenieLekara);
		addOwnMessage(Mc.koniecZahrievania);
		addOwnMessage(Mc.uvolnenieLekara);
	}
	//meta! tag="end"

    public Lekar vyberVolnehoLekara(Ambulancia cielovaAmbulancia)
    {
        StrategiaLekarov strategia =
                ((MySimulation) mySim()).getStrategiaLekarov();

        switch (strategia) {
            case PRVY_VOLNY_LEKAR:
                return vyberPrvehoLekara();

            case LEKAR_V_AMBULANCII_INAK_PRVY_VOLNY:
                return vyberVolnehoLekaraMinimalnyPresun(cielovaAmbulancia);

            case LEKAR_V_AMBULANCII_INAK_NAJDLHSIE_VOLNY:
                return vyberVolnehoLekaraNajdlhsieVolny(cielovaAmbulancia);

            default:
                return vyberPrvehoLekara();
        }
    }

    private Lekar vyberPrvehoLekara()
    {
        for (Lekar lekar : lekari) {
            if (!lekar.jeObsadeny()) {
                lekar.setJeObsadeny(true);
                lekar.setCasOdKedyJeVolny(0.0);
                return lekar;
            }
        }
        return null;
    }

    public List<Lekar> getLekari() {
        return lekari;
    }

    public void uvolniLekara(Lekar lekar)
    {
        if (lekar != null) {
            lekar.setJeObsadeny(false);
            lekar.setCasOdKedyJeVolny(mySim().currentTime());
            lekar.setAktualnyPacient(null);
            if (lekar.getAktualnaAmbulancia() != null) {
                lekar.setPoloha(lekar.getAktualnaAmbulancia());
            }
            lekar.setAktualnaAmbulancia(null);
        }
    }

    private Lekar vyberVolnehoLekaraMinimalnyPresun(Ambulancia cielovaAmbulancia) {
        Lekar vystup = null;

        for (Lekar lekar : lekari) {
            if (lekar.jeObsadeny()) {
                continue;
            }

            if (lekar.getPoloha() == cielovaAmbulancia) {
                lekar.setJeObsadeny(true);
                lekar.setCasOdKedyJeVolny(0.0);
                return lekar;
            }

            if (vystup == null || lekar.id() < vystup.id()) {
                vystup = lekar;
            }
        }

        if (vystup != null) {
            vystup.setJeObsadeny(true);
            vystup.setCasOdKedyJeVolny(0.0);
        }

        return vystup;
    }

    private Lekar vyberVolnehoLekaraNajdlhsieVolny(Ambulancia cielovaAmbulancia) {
        Lekar najdlhsieVolny = null;

        for (Lekar lekar : lekari) {
            if (lekar.jeObsadeny()) {
                continue;
            }

            if (lekar.getPoloha() == cielovaAmbulancia) {
                lekar.setJeObsadeny(true);
                lekar.setCasOdKedyJeVolny(0.0);
                return lekar;
            }

            if (najdlhsieVolny == null
                    || lekar.getCasOdKedyJeVolny() < najdlhsieVolny.getCasOdKedyJeVolny()
                    || (lekar.getCasOdKedyJeVolny() == najdlhsieVolny.getCasOdKedyJeVolny()
                    && lekar.id() < najdlhsieVolny.id())) {
                najdlhsieVolny = lekar;
            }
        }

        if (najdlhsieVolny != null) {
            najdlhsieVolny.setJeObsadeny(true);
            najdlhsieVolny.setCasOdKedyJeVolny(0.0);
        }

        return najdlhsieVolny;
    }

    public int getPocetObsadenychLekarov() {
        int pocet = 0;

        for (Lekar lekar : lekari) {
            if (lekar.jeObsadeny()) {
                pocet++;
            }
        }

        return pocet;
    }

    public void aktualizujVytazenieLekarov() {
        MySimulation sim = (MySimulation) mySim();
        vytazenieLekarovStat.update((double) getPocetObsadenychLekarov() / sim.getPocetLekarov(), mySim().currentTime());
    }

    public TimeWeightedStatistic getVytazenieLekarovStat() {
        return vytazenieLekarovStat;
    }

    public void resetStatistikyPoZahrievani() {
        MySimulation sim = (MySimulation) mySim();

        double vytazenie = 0.0;
        if (sim.getPocetLekarov() > 0) {
            vytazenie = (double) getPocetObsadenychLekarov() / sim.getPocetLekarov();
        }

        vytazenieLekarovStat.reset(mySim().currentTime(), vytazenie);
    }
}
