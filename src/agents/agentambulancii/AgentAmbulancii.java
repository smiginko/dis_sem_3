package agents.agentambulancii;

import OSPABA.*;
import agents.agenturgentu.ManagerUrgentu;
import entity.Lekar;
import entity.Sestra;
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
		addOwnMessage(Mc.koniecZahrievania);
		addOwnMessage(Mc.uvolnenieAmbulancie);
	}
	//meta! tag="end"

    public Ambulancia vyberVolnuAmbulanciu(MyMessage msg)
    {
        StrategiaPridelovania strategia =
                ((MySimulation) mySim()).getStrategiaPridelovania();

        switch (strategia) {
            case PRVA_VOLNA:
                return vyberPrvuVolnuAmbulanciu(msg.isPovolenaAmbulanciaA(), msg.isPovolenaAmbulanciaB());

            case NAJDLHSIE_VOLNA:
                return null;

            case MINIMALNY_PRESUN_VYVAZENE_RADY:
                return vyberMinimalnyPresunVyvazeneRady(msg);


            default:
                return vyberPrvuVolnuAmbulanciu(msg.isPovolenaAmbulanciaA(), msg.isPovolenaAmbulanciaB());
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

    private Ambulancia vyberMinimalnyPresunVyvazeneRady(MyMessage msg) {
        List<Ambulancia> kandidati = volneKompatibilneAmbulancie(
                msg.isPovolenaAmbulanciaA(),
                msg.isPovolenaAmbulanciaB()
        );

        if (kandidati.isEmpty()) {
            return null;
        }

        if (msg.isPovolenaAmbulanciaA() && msg.isPovolenaAmbulanciaB()) {
            Ambulancia.TypeAmbulancia preferovanyTyp = preferovanyTypPreFlexibilneho();
            List<Ambulancia> preferovane = new ArrayList<>();

            for (Ambulancia ambulancia : kandidati) {
                if (ambulancia.getTyp() == preferovanyTyp) {
                    preferovane.add(ambulancia);
                }
            }

            if (!preferovane.isEmpty()) {
                kandidati = preferovane;
            }
        }

        Ambulancia najlepsia = null;
        int najlepsiScore = Integer.MIN_VALUE;

        for (Ambulancia ambulancia : kandidati) {
            int score = scoreAmbulanciePrePresun(ambulancia, msg);

            if (najlepsia == null
                    || score > najlepsiScore
                    || (score == najlepsiScore && ambulancia.id() < najlepsia.id())) {
                najlepsia = ambulancia;
                najlepsiScore = score;
            }
        }

        najlepsia.setJeObsadena(true);
        return najlepsia;
    }

    private int scoreAmbulanciePrePresun(Ambulancia ambulancia, MyMessage msg) {
        int score = 0;

        score += pocetVolnychSestierNa(ambulancia) * 10;

        if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
            score += pocetVolnychLekarovNa(ambulancia) * 10;
        }

        return score;
    }

    private int pocetVolnychSestierNa(Ambulancia ambulancia) {
        int pocet = 0;
        MySimulation sim = (MySimulation) mySim();

        for (Sestra sestra : sim.agentSestier().getSestry()) {
            if (!sestra.jeObsadena() && sestra.getPoloha() == ambulancia) {
                pocet++;
            }
        }

        return pocet;
    }

    private int pocetVolnychLekarovNa(Ambulancia ambulancia) {
        int pocet = 0;
        MySimulation sim = (MySimulation) mySim();

        for (Lekar lekar : sim.agentLekarov().getLekari()) {
            if (!lekar.jeObsadeny() && lekar.getPoloha() == ambulancia) {
                pocet++;
            }
        }

        return pocet;
    }

    private Ambulancia.TypeAmbulancia preferovanyTypPreFlexibilneho() {
        double tlakA = (double) pocetCakajucichVyhradneNaA()
                / Math.max(1, pocetVolnychAmbulancii(Ambulancia.TypeAmbulancia.TYP_A));

        double tlakB = (double) pocetCakajucichVyhradneNaB()
                / Math.max(1, pocetVolnychAmbulancii(Ambulancia.TypeAmbulancia.TYP_B));

        if (tlakA < tlakB) {
            return Ambulancia.TypeAmbulancia.TYP_A;
        }

        return Ambulancia.TypeAmbulancia.TYP_B;
    }

    private int pocetVolnychAmbulancii(Ambulancia.TypeAmbulancia typ) {
        int pocet = 0;

        for (Ambulancia ambulancia : ambulancie) {
            if (!ambulancia.isJeObsadena() && ambulancia.getTyp() == typ) {
                pocet++;
            }
        }

        return pocet;
    }

    private int pocetCakajucichVyhradneNaA() {
        return pocetVoFrontoch(true, false);
    }

    private int pocetCakajucichVyhradneNaB() {
        return pocetVoFrontoch(false, true);
    }

    private int pocetVoFrontoch(boolean lenA, boolean lenB) {
        int pocet = 0;
        ManagerUrgentu urgent = (ManagerUrgentu) ((MySimulation) mySim()).agentUrgentu().myManager();

        for (MyMessage msg : urgent.getRadNaVstupneVysetrenie())
            if (msg.isPovolenaAmbulanciaA() == lenA && msg.isPovolenaAmbulanciaB() == lenB) pocet++;

        for (MyMessage msg : urgent.getRadNaOsetrenie())
            if (msg.isPovolenaAmbulanciaA() == lenA && msg.isPovolenaAmbulanciaB() == lenB) pocet++;

        for (MyMessage msg : ((ManagerAmbulancii) myManager()).getRadCakajucich())
            if (msg.isPovolenaAmbulanciaA() == lenA && msg.isPovolenaAmbulanciaB() == lenB) pocet++;

        return pocet;
    }


    private List<Ambulancia> volneKompatibilneAmbulancie(boolean povolenaA, boolean povolenaB) {
        List<Ambulancia> kandidati = new ArrayList<>();

        for (Ambulancia ambulancia : ambulancie) {
            if (ambulancia.isJeObsadena()) {
                continue;
            }

            boolean jeA = ambulancia.getTyp() == Ambulancia.TypeAmbulancia.TYP_A;
            boolean jeB = ambulancia.getTyp() == Ambulancia.TypeAmbulancia.TYP_B;

            if ((povolenaA && jeA) || (povolenaB && jeB)) {
                kandidati.add(ambulancia);
            }
        }

        return kandidati;
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

    public int getPocetObsadenychAmbulanciiA() {
        int pocet = 0;

        for (Ambulancia ambulancia : ambulancie) {
            if (ambulancia.getTyp() == Ambulancia.TypeAmbulancia.TYP_A
                    && ambulancia.isJeObsadena()) {
                pocet++;
            }
        }

        return pocet;
    }

    public int getPocetObsadenychAmbulanciiB() {
        int pocet = 0;

        for (Ambulancia ambulancia : ambulancie) {
            if (ambulancia.getTyp() == Ambulancia.TypeAmbulancia.TYP_B
                    && ambulancia.isJeObsadena()) {
                pocet++;
            }
        }

        return pocet;
    }
}