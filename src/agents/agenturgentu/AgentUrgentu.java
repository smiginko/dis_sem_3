package agents.agenturgentu;

import OSPABA.*;
import simulation.*;
import agents.agenturgentu.continualassistants.*;
import statistiky.Statistic;
import statistiky.TimeWeightedStatistic;

import java.util.List;

//meta! id="14"
public class AgentUrgentu extends OSPABA.Agent
{
    private TimeWeightedStatistic dlzkaRaduVstupne;
    private TimeWeightedStatistic dlzkaRaduOsetrenie;
    private int pocetCakajucichNaVstupne;
    private int pocetCakajucichNaOsetrenie;
    private Statistic casVCakarniVstupnePesoStat;
    private Statistic casVCakarniVstupneSanitkaStat;
    private Statistic casOdVstupuPoZaciatokOsetreniaSanitkaStat;
    private Statistic casOdVstupuPoZaciatokOsetreniaPesoStat;
    private Statistic[] casVCakarniOsetreniePrioritaStat;

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
        pocetCakajucichNaVstupne = 0;
        pocetCakajucichNaOsetrenie = 0;

        dlzkaRaduVstupne = new TimeWeightedStatistic(
                "Priemerna dlzka radu na vstupne vysetrenie",
                mySim().currentTime(),
                0
        );

        dlzkaRaduOsetrenie = new TimeWeightedStatistic(
                "Priemerna dlzka radu na osetrenie",
                mySim().currentTime(),
                0
        );

        casVCakarniVstupnePesoStat = new Statistic("Cas cakania na vstupne vysetrenie - PESO");
        casVCakarniVstupneSanitkaStat = new Statistic("Cas cakania na vstupne vysetrenie - SANITKA");

        casVCakarniOsetreniePrioritaStat = new Statistic[6];
        for (int priorita = 1; priorita <= 5; priorita++) {
            casVCakarniOsetreniePrioritaStat[priorita] =
                    new Statistic("Cas cakania na osetrenie - priorita " + priorita);
        }

        casOdVstupuPoZaciatokOsetreniaSanitkaStat =
                new Statistic("Cas od vstupu po zaciatok osetrenia - SANITKA");

        casOdVstupuPoZaciatokOsetreniaPesoStat =
                new Statistic("Cas od vstupu po zaciatok osetrenia - PESO");
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

    //Statistiky-------
    public void zapisCakanieNaVstupneVysetrenie(MyMessage msg) {
        double cakanie = mySim().currentTime() - msg.getCasVstupuDoAktualnehoRadu();

        switch (msg.getPacient().getTyp()) {
            case PESO:
                casVCakarniVstupnePesoStat.addValue(cakanie);
                break;
            case SANITKA:
                casVCakarniVstupneSanitkaStat.addValue(cakanie);
                break;
            default:
                throw new IllegalStateException("Neznamy typ pacienta: " + msg.getPacient().getTyp());
        }
    }

    public void zapisCakanieNaOsetrenie(MyMessage msg) {
        int priorita = msg.getPacient().getPriorita();

        if (priorita < 1 || priorita > 5) {
            throw new IllegalStateException("Neplatna priorita pacienta: " + priorita);
        }

        double cakanie = mySim().currentTime() - msg.getCasVstupuDoAktualnehoRadu();
        casVCakarniOsetreniePrioritaStat[priorita].addValue(cakanie);
    }

    public void zapisCasOdVstupuPoZaciatokOsetrenia(MyMessage msg) {
        MySimulation sim = (MySimulation) mySim();

        if (msg.getPacient().getCasPrichodu() < sim.getWarmupTime()) {
            return;
        }

        double cas = mySim().currentTime() - msg.getPacient().getCasPrichodu();

        switch (msg.getPacient().getTyp()) {
            case SANITKA:
                casOdVstupuPoZaciatokOsetreniaSanitkaStat.addValue(cas);
                break;
            case PESO:
                casOdVstupuPoZaciatokOsetreniaPesoStat.addValue(cas);
                break;
            default:
                throw new IllegalStateException("Neznamy typ pacienta: " + msg.getPacient().getTyp());
        }
    }

    public void zacalCakatNaVstupne() {
        pocetCakajucichNaVstupne++;
        dlzkaRaduVstupne.update(pocetCakajucichNaVstupne, mySim().currentTime());
    }

    public void prestalCakatNaVstupne() {
        if (pocetCakajucichNaVstupne <= 0) {
            throw new IllegalStateException("Pocet cakajucich na vstupne nemoze byt zaporny");
        }

        pocetCakajucichNaVstupne--;
        dlzkaRaduVstupne.update(pocetCakajucichNaVstupne, mySim().currentTime());
    }

    public void zacalCakatNaOsetrenie() {
        pocetCakajucichNaOsetrenie++;
        dlzkaRaduOsetrenie.update(pocetCakajucichNaOsetrenie, mySim().currentTime());
    }

    public void prestalCakatNaOsetrenie() {
        if (pocetCakajucichNaOsetrenie <= 0) {
            throw new IllegalStateException("Pocet cakajucich na osetrenie nemoze byt zaporny");
        }

        pocetCakajucichNaOsetrenie--;
        dlzkaRaduOsetrenie.update(pocetCakajucichNaOsetrenie, mySim().currentTime());
    }


    public TimeWeightedStatistic getDlzkaRaduVstupne() {
        return dlzkaRaduVstupne;
    }

    public TimeWeightedStatistic getDlzkaRaduOsetrenie() {
        return dlzkaRaduOsetrenie;
    }

    public Statistic getCasVCakarniVstupnePesoStat() {
        return casVCakarniVstupnePesoStat;
    }

    public Statistic getCasVCakarniVstupneSanitkaStat() {
        return casVCakarniVstupneSanitkaStat;
    }

    public Statistic getCasVCakarniOsetreniePriorita(int priorita) {
        return casVCakarniOsetreniePrioritaStat[priorita];
    }

    public Statistic getCasOdVstupuPoZaciatokOsetreniaSanitkaStat() {
        return casOdVstupuPoZaciatokOsetreniaSanitkaStat;
    }

    public Statistic getCasOdVstupuPoZaciatokOsetreniaPesoStat() {
        return casOdVstupuPoZaciatokOsetreniaPesoStat;
    }

    public void resetStatistikyPoZahrievani() {
        double now = mySim().currentTime();

        dlzkaRaduVstupne.reset(now, pocetCakajucichNaVstupne);
        dlzkaRaduOsetrenie.reset(now, pocetCakajucichNaOsetrenie);

        casVCakarniVstupnePesoStat.reset();
        casVCakarniVstupneSanitkaStat.reset();

        for (int priorita = 1; priorita <= 5; priorita++) {
            casVCakarniOsetreniePrioritaStat[priorita].reset();
        }

        casOdVstupuPoZaciatokOsetreniaSanitkaStat.reset();
        casOdVstupuPoZaciatokOsetreniaPesoStat.reset();
    }
}