package agents.agenturgentu;

import OSPABA.*;
import entity.Ambulancia;
import entity.Lekar;
import entity.Pacient;
import entity.Sestra;
import simulation.*;
import statistiky.Statistic;
import statistiky.TimeWeightedStatistic;

import java.util.*;

//meta! id="14"
public class ManagerUrgentu extends OSPABA.Manager
{

    private PriorityQueue<MyMessage> radNaVstupneVysetrenie;
    private PriorityQueue<MyMessage> radNaOsetrenie;

    private TimeWeightedStatistic dlzkaRaduVstupne;
    private TimeWeightedStatistic dlzkaRaduOsetrenie;

    private int pocetCakajucichNaVstupne;
    private int pocetCakajucichNaOsetrenie;

    private Statistic casVCakarniVstupnePesoStat;
    private Statistic casVCakarniVstupneSanitkaStat;
    private Statistic[] casVCakarniOsetreniePrioritaStat;

    private int pocetPacientovVSysteme = 0;

	public ManagerUrgentu(int id, Simulation mySim, Agent myAgent)
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

        radNaVstupneVysetrenie = new PriorityQueue<>(MyMessage.PORADIE);
        radNaOsetrenie = new PriorityQueue<>(MyMessage.PORADIE);

        pocetCakajucichNaVstupne = 0;
        pocetCakajucichNaOsetrenie = 0;
        pocetPacientovVSysteme = 0;

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

        if (((MySimulation) mySim()).isCollectAnalysisData()) {
            MyMessage msg = new MyMessage(mySim());
            msg.setAddressee(Id.peciatkaZahrievania);
            startContinualAssistant(msg);
        }
	}

	//meta! sender="HlavnyAgent", id="15", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="AgentOsetrenia", id="27", type="Response"
	public void processOsetreniePacienta(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        uvolniSestruPreMsg(msg);
        uvolniLekaraPreMsg(msg);
        uvolniAmbulanciuPreMsg(msg);

        message.setAddressee(Id.presunZCakarne);
        startContinualAssistant(message);

        skusSpustitOsetrenie();
        skusSpustitVstupneVysetrenie();
	}

	//meta! sender="AgentSestier", id="44", type="Response"
	public void processPridelenieSestry(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        nastavSestruPacientovi(msg);

        if (msg.getFazaPacienta() == MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE) {

            ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id()
                    + " ZAČÍNA vstupné vyšetrenie amb=" + msg.getAmbulancia().id()
                    + " sestra=" + msg.getSestra().id());

            zapisCakanieNaVstupneVysetrenie(msg);
            prestalCakatNaVstupne();

            message.setCode(Mc.vstupneVysetreniePacienta);
            message.setAddressee(Id.agentVstupnehoVysetrenia);
            request(message);
        }

        if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {

            ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id()
                    + " ZAČÍNA ošetrenie amb=" + msg.getAmbulancia().id()
                    + " lekár=" + msg.getLekar().id()
                    + " sestra=" + msg.getSestra().id()
                    + " priorita=" + msg.getPacient().getPriorita());

            zapisCakanieNaOsetrenie(msg);
            prestalCakatNaOsetrenie();

            message.setCode(Mc.osetreniePacienta);
            message.setAddressee(Id.agentOsetrenia);
            request(message);
        }
	}

	//meta! sender="AgentAmbulancii", id="32", type="Response"
	public void processPridelenieAmbulancie(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        msg.getPacient().setAktualnaAmbulancia(msg.getAmbulancia());

        if (msg.getFazaPacienta() == MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE) {

            message.setCode(Mc.pridelenieSestry);
            message.setAddressee(Id.agentSestier);
            request(message);
        }

        if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {

            message.setCode(Mc.pridelenieLekara);
            message.setAddressee(Id.agentLekarov);
            request(message);
        }
	}

	//meta! sender="AgentLekarov", id="51", type="Response"
	public void processPridelenieLekara(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        Pacient pacient = msg.getPacient();
        Lekar lekar = msg.getLekar();
        Ambulancia ambulancia = msg.getAmbulancia();

        pacient.setAktualnyLekar(lekar);
        lekar.setAktualnyPacient(pacient);
        lekar.setAktualnaAmbulancia(ambulancia);

        message.setCode(Mc.pridelenieSestry);
        message.setAddressee(Id.agentSestier);
        request(message);
	}

	//meta! sender="HlavnyAgent", id="18", type="Request"
	public void processObsluhaPacienta(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        ((MySimulation) mySim()).log("Urgent prevzal pacienta id=" + msg.getPacient().id()
                + " typ=" + msg.getPacient().getTyp());

        pocetPacientovVSysteme++;

        msg.setFazaPacienta(MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE);
        msg.setPovolenaAmbulanciaA(false);
        msg.setPovolenaAmbulanciaB(true);

        message.setAddressee(Id.presunDoCakarne);
        startContinualAssistant(message);
	}

	//meta! sender="AgentVstupnehoVysetrenia", id="22", type="Response"
	public void processVstupneVysetreniePacienta(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        uvolniSestruPreMsg(msg);
        uvolniAmbulanciuPreMsg(msg);

        ((MySimulation)mySim()).animaciaUrgentu().presunPacientaDoCakarne(msg.getPacient(), 0.0);

        msg.setFazaPacienta(MyMessage.FazaPacienta.OSETRENIE);
        nastavPovoleneAmbulanciePreOsetrenie(msg);

        zacalCakatNaOsetrenie();
        vlozDoRaduOsetrenie(msg);
        ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id()
                + " vstúpil do frontu na ošetrenie (priorita " + msg.getPacient().getPriorita() + ")");

        skusSpustitOsetrenie();
        skusSpustitVstupneVysetrenie();
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! sender="PresunDoCakarne", id="108", type="Finish"
	public void processFinishPresunDoCakarne(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        switch (message.sender().id()) {
            case Id.presunDoCakarne:
                zacalCakatNaVstupne();
                vlozDoRaduVstupne(msg);
                ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id() + " prišiel do čakárne");
                skusSpustitVstupneVysetrenie();
                break;

            default:
                processDefault(message);
                break;
        }
	}

	//meta! sender="PresunZCakarne", id="115", type="Finish"
	public void processFinishPresunZCakarne(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        ((MySimulation) mySim()).log("Pacient id=" + msg.getPacient().id() + " dokončil odchod z urgentu");

        pocetPacientovVSysteme--;

        message.setCode(Mc.obsluhaPacienta);
        response(message);
	}

	//meta! sender="HlavnyAgent", id="140", type="Notice"
	public void processKoniecZahrievania(MessageForm message)
	{
        double now = mySim().currentTime();
        dlzkaRaduVstupne.reset(now, 0);
        dlzkaRaduOsetrenie.reset(now, 0);
        casVCakarniVstupnePesoStat.reset();
        casVCakarniVstupneSanitkaStat.reset();
        for (int i = 1; i <= 5; i++) casVCakarniOsetreniePrioritaStat[i].reset();

        for (int agentId : new int[]{Id.agentSestier, Id.agentLekarov, Id.agentAmbulancii}) {
            MyMessage m = new MyMessage(mySim());
            m.setCode(Mc.koniecZahrievania);
            m.setAddressee(agentId);
            notice(m);
        }
	}

	//meta! sender="PeciatkaZahrievania", id="151", type="Finish"
	public void processFinishPeciatkaZahrievania(MessageForm message)
	{
        ((MySimulation) mySim()).recordSnapshot(pocetPacientovVSysteme);

        message.setAddressee(Id.peciatkaZahrievania);
        startContinualAssistant(message);
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
		case Mc.koniecZahrievania:
			processKoniecZahrievania(message);
		break;

		case Mc.finish:
			switch (message.sender().id())
			{
			case Id.presunDoCakarne:
				processFinishPresunDoCakarne(message);
			break;

			case Id.peciatkaZahrievania:
				processFinishPeciatkaZahrievania(message);
			break;

			case Id.presunZCakarne:
				processFinishPresunZCakarne(message);
			break;
			}
		break;

		case Mc.init:
			processInit(message);
		break;

		case Mc.obsluhaPacienta:
			processObsluhaPacienta(message);
		break;

		case Mc.osetreniePacienta:
			processOsetreniePacienta(message);
		break;

		case Mc.vstupneVysetreniePacienta:
			processVstupneVysetreniePacienta(message);
		break;

		case Mc.pridelenieSestry:
			processPridelenieSestry(message);
		break;

		case Mc.pridelenieAmbulancie:
			processPridelenieAmbulancie(message);
		break;

		case Mc.pridelenieLekara:
			processPridelenieLekara(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentUrgentu myAgent()
	{
		return (AgentUrgentu)super.myAgent();
	}

    private void nastavPovoleneAmbulanciePreOsetrenie(MyMessage msg)
    {
        int priorita = msg.getPacient().getPriorita();

        if (priorita == 1 || priorita == 2) {
            msg.setPovolenaAmbulanciaA(true);
            msg.setPovolenaAmbulanciaB(false);
        } else if (priorita == 5) {
            msg.setPovolenaAmbulanciaA(false);
            msg.setPovolenaAmbulanciaB(true);
        } else {
            msg.setPovolenaAmbulanciaA(true);
            msg.setPovolenaAmbulanciaB(true);
        }
    }

    private void skusSpustitVstupneVysetrenie() {
        MyMessage msg = vyberZRaduVstupne();

        if (msg == null) {
            return;
        }

        msg.setCode(Mc.pridelenieAmbulancie);
        msg.setAddressee(Id.agentAmbulancii);
        request(msg);
    }

    private void skusSpustitOsetrenie() {
        MyMessage msg = vyberZRaduOsetrenie();

        if (msg == null) {
            return;
        }

        msg.setCode(Mc.pridelenieAmbulancie);
        msg.setAddressee(Id.agentAmbulancii);
        request(msg);
    }

    private void nastavSestruPacientovi(MyMessage msg) {
        Pacient pacient = msg.getPacient();
        Sestra sestra = msg.getSestra();
        Ambulancia ambulancia = msg.getAmbulancia();

        pacient.setAktualnaSestra(sestra);
        sestra.setAktualnyPacient(pacient);
        sestra.setAktualnaAmbulancia(ambulancia);
    }

    //-----------

    private void uvolniAmbulanciuPreMsg(MyMessage msg) {
        if (msg.getAmbulancia() == null) {
            return;
        }

        MyMessage uvolnenie = new MyMessage(mySim());
        uvolnenie.setCode(Mc.uvolnenieAmbulancie);
        uvolnenie.setAddressee(Id.agentAmbulancii);
        uvolnenie.setAmbulancia(msg.getAmbulancia());
        notice(uvolnenie);

        msg.getPacient().setAktualnaAmbulancia(null);
        msg.setAmbulancia(null);
    }

    private void uvolniLekaraPreMsg(MyMessage msg) {
        if (msg.getLekar() == null) {
            return;
        }

        MyMessage uvolnenie = new MyMessage(mySim());
        uvolnenie.setCode(Mc.uvolnenieLekara);
        uvolnenie.setAddressee(Id.agentLekarov);
        uvolnenie.setLekar(msg.getLekar());
        notice(uvolnenie);

        msg.getPacient().setAktualnyLekar(null);
        msg.setLekar(null);
    }

    private void uvolniSestruPreMsg(MyMessage msg) {
        if (msg.getSestra() == null) {
            return;
        }

        MyMessage uvolnenie = new MyMessage(mySim());
        uvolnenie.setCode(Mc.uvolnenieSestry);
        uvolnenie.setAddressee(Id.agentSestier);
        uvolnenie.setSestra(msg.getSestra());
        notice(uvolnenie);

        msg.getPacient().setAktualnaSestra(null);
        msg.setSestra(null);
    }

    //Rady---------
    private void vlozDoRaduVstupne(MyMessage msg) {
        msg.setCasVstupuDoAktualnehoRadu(mySim().currentTime());
        radNaVstupneVysetrenie.offer(msg);
    }

    private MyMessage vyberZRaduVstupne() {
        MyMessage msg = radNaVstupneVysetrenie.poll();
        return msg;
    }

    private void vlozDoRaduOsetrenie(MyMessage msg) {
        int priorita = msg.getPacient().getPriorita();

        if (priorita < 1 || priorita > 5) {
            throw new IllegalStateException("Pacient id=" + msg.getPacient().id()
                    + " ma neplatnu prioritu pre osetrenie: " + priorita);
        }

        msg.setCasVstupuDoAktualnehoRadu(mySim().currentTime());
        radNaOsetrenie.offer(msg);
    }

    private MyMessage vyberZRaduOsetrenie() {
        MyMessage msg = radNaOsetrenie.poll();
        return msg;
    }

    public List<MyMessage> getRadNaVstupneVysetrenie() {
        List<MyMessage> sorted = new ArrayList<>(radNaVstupneVysetrenie);
        sorted.sort(MyMessage.PORADIE);
        return sorted;
    }

    public List<MyMessage> getRadNaOsetrenie() {
        List<MyMessage> sorted = new ArrayList<>(radNaOsetrenie);
        sorted.sort(MyMessage.PORADIE);
        return sorted;
    }

    //Statistiky-------
    private void zapisCakanieNaVstupneVysetrenie(MyMessage msg) {
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

    private void zapisCakanieNaOsetrenie(MyMessage msg) {
        int priorita = msg.getPacient().getPriorita();

        if (priorita < 1 || priorita > 5) {
            throw new IllegalStateException("Neplatna priorita pacienta: " + priorita);
        }

        double cakanie = mySim().currentTime() - msg.getCasVstupuDoAktualnehoRadu();
        casVCakarniOsetreniePrioritaStat[priorita].addValue(cakanie);
    }

    private void zacalCakatNaVstupne() {
        pocetCakajucichNaVstupne++;
        dlzkaRaduVstupne.update(pocetCakajucichNaVstupne, mySim().currentTime());
    }

    private void prestalCakatNaVstupne() {
        if (pocetCakajucichNaVstupne <= 0) {
            throw new IllegalStateException("Pocet cakajucich na vstupne nemoze byt zaporny");
        }

        pocetCakajucichNaVstupne--;
        dlzkaRaduVstupne.update(pocetCakajucichNaVstupne, mySim().currentTime());
    }

    private void zacalCakatNaOsetrenie() {
        pocetCakajucichNaOsetrenie++;
        dlzkaRaduOsetrenie.update(pocetCakajucichNaOsetrenie, mySim().currentTime());
    }

    private void prestalCakatNaOsetrenie() {
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
}