package agents.agenturgentu;

import OSPABA.*;
import entity.Ambulancia;
import entity.Lekar;
import entity.Pacient;
import entity.Sestra;
import simulation.*;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

//meta! id="14"
public class ManagerUrgentu extends OSPABA.Manager
{

    private Deque<MyMessage> radNaVstupneVysetrenie;
    private List<Deque<MyMessage>> radyNaOsetrenie;

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

        radNaVstupneVysetrenie = new LinkedList<>();

        radyNaOsetrenie = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            radyNaOsetrenie.add(new LinkedList<>());
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

        message.setCode(Mc.obsluhaPacienta);
        response(message);

        skusSpustitOsetrenie();
        skusSpustitVstupneVysetrenie();
	}

	//meta! sender="AgentSestier", id="44", type="Response"
	public void processPridelenieSestry(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        if (msg.getFazaPacienta() == MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE) {
            if (msg.getSestra() == null) {
                uvolniAmbulanciuPreMsg(msg);
                vratNaZaciatokRaduVstupne(msg);
                return;
            }

            nastavSestruPacientovi(msg);

            message.setCode(Mc.vstupneVysetreniePacienta);
            message.setAddressee(Id.agentVstupnehoVysetrenia);
            request(message);
        }

        if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
            if (msg.getSestra() == null) {
                uvolniLekaraPreMsg(msg);
                uvolniAmbulanciuPreMsg(msg);
                vratNaZaciatokRaduOsetrenie(msg);
                return;
            }

            nastavSestruPacientovi(msg);

            message.setCode(Mc.osetreniePacienta);
            message.setAddressee(Id.agentOsetrenia);
            request(message);
        }
	}

	//meta! sender="AgentAmbulancii", id="32", type="Response"
	public void processPridelenieAmbulancie(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        if (msg.getAmbulancia() == null) {
            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE) {
                vratNaZaciatokRaduVstupne(msg);
                return;
            }

            if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
                vratNaZaciatokRaduOsetrenie(msg);
                return;
            }
        }

        msg.getPacient().setAktualnaAmbulancia(msg.getAmbulancia());

        if (msg.getFazaPacienta() == MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE) {
            System.out.println("Pacient id=" + msg.getPacient().id()
                    + " dostal ambulanciu na vstupne vysetrenie id="
                    + msg.getAmbulancia().id());

            message.setCode(Mc.pridelenieSestry);
            message.setAddressee(Id.agentSestier);
            request(message);
            return;
        }

        if (msg.getFazaPacienta() == MyMessage.FazaPacienta.OSETRENIE) {
            System.out.println("Pacient id=" + msg.getPacient().id()
                    + " dostal ambulanciu na osetrenie id="
                    + msg.getAmbulancia().id()
                    + " priorita="
                    + msg.getPacient().getPriorita());

            message.setCode(Mc.pridelenieLekara);
            message.setAddressee(Id.agentLekarov);
            request(message);
        }
	}

	//meta! sender="AgentLekarov", id="51", type="Response"
	public void processPridelenieLekara(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        if (msg.getLekar() == null) {
            uvolniAmbulanciuPreMsg(msg);
            vratNaZaciatokRaduOsetrenie(msg);
            return;
        }

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

        System.out.println("Urgent prevzal pacienta id="
                + msg.getPacient().id()
                + " typ="
                + msg.getPacient().getTyp());

        msg.setFazaPacienta(MyMessage.FazaPacienta.VSTUPNE_VYSETRENIE);
        msg.setPovolenaAmbulanciaA(false);
        msg.setPovolenaAmbulanciaB(true);

        vlozDoRaduVstupne(msg);
        skusSpustitVstupneVysetrenie();
	}

	//meta! sender="AgentVstupnehoVysetrenia", id="22", type="Response"
	public void processVstupneVysetreniePacienta(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        uvolniSestruPreMsg(msg);
        uvolniAmbulanciuPreMsg(msg);

        msg.setFazaPacienta(MyMessage.FazaPacienta.OSETRENIE);
        nastavPovoleneAmbulanciePreOsetrenie(msg);

        vlozDoRaduOsetrenie(msg);

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

	//meta! userInfo="Generated code: do not modify", tag="begin"
	public void init()
	{
	}

	@Override
	public void processMessage(MessageForm message)
	{
		switch (message.code())
		{
		case Mc.pridelenieSestry:
			processPridelenieSestry(message);
		break;

		case Mc.vstupneVysetreniePacienta:
			processVstupneVysetreniePacienta(message);
		break;

		case Mc.osetreniePacienta:
			processOsetreniePacienta(message);
		break;

		case Mc.pridelenieLekara:
			processPridelenieLekara(message);
		break;

		case Mc.obsluhaPacienta:
			processObsluhaPacienta(message);
		break;

		case Mc.pridelenieAmbulancie:
			processPridelenieAmbulancie(message);
		break;

		case Mc.init:
			processInit(message);
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

    //-----------Rady
    private void vlozDoRaduVstupne(MyMessage msg) {
        msg.setCasVstupuDoAktualnehoRadu(mySim().currentTime());
        radNaVstupneVysetrenie.addLast(msg);
    }

    private void vratNaZaciatokRaduVstupne(MyMessage msg) {
        radNaVstupneVysetrenie.addFirst(msg);
    }

    private MyMessage vyberZRaduVstupne() {
        MyMessage msg = radNaVstupneVysetrenie.pollFirst();

        if (msg != null) {
            double cakanie = mySim().currentTime() - msg.getCasVstupuDoAktualnehoRadu();
            // TODO: STAT čakanie na vstupne vysetrenie
        }

        return msg;
    }

    private void vlozDoRaduOsetrenie(MyMessage msg) {
        msg.setCasVstupuDoAktualnehoRadu(mySim().currentTime());
        radyNaOsetrenie.get(msg.getPacient().getPriorita()).addLast(msg);
    }

    private void vratNaZaciatokRaduOsetrenie(MyMessage msg) {
        radyNaOsetrenie.get(msg.getPacient().getPriorita()).addFirst(msg);
    }

    private MyMessage vyberZRaduOsetrenie() {
        for (int priorita = 1; priorita <= 5; priorita++) {
            MyMessage msg = radyNaOsetrenie.get(priorita).pollFirst();

            if (msg != null) {
                double cakanie = mySim().currentTime() - msg.getCasVstupuDoAktualnehoRadu();
                // TODO: STAT čakanie na ošetrenie
                return msg;
            }
        }

        return null;
    }
}
