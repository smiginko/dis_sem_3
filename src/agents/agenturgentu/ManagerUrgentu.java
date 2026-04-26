package agents.agenturgentu;

import OSPABA.*;
import entity.Ambulancia;
import entity.Pacient;
import entity.Sestra;
import simulation.*;

//meta! id="14"
public class ManagerUrgentu extends OSPABA.Manager
{
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
	}

	//meta! sender="HlavnyAgent", id="15", type="Notice"
	public void processInit(MessageForm message)
	{
	}

	//meta! sender="AgentOsetrenia", id="27", type="Response"
	public void processOsetreniePacienta(MessageForm message)
	{
	}

	//meta! sender="AgentSestier", id="44", type="Response"
	public void processPridelenieSestry(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        if (msg.getSestra() == null) {
            System.out.println("Pacient caka, nie je volna sestra id="
                    + msg.getPacient().id());

            MyMessage uvolnenieAmbulancie = new MyMessage(mySim());
            uvolnenieAmbulancie.setCode(Mc.uvolnenieAmbulancie);
            uvolnenieAmbulancie.setAddressee(Id.agentAmbulancii);
            uvolnenieAmbulancie.setAmbulancia(msg.getAmbulancia());
            notice(uvolnenieAmbulancie);

            msg.getPacient().setAktualnaAmbulancia(null);
            msg.setAmbulancia(null);

            message.setCode(Mc.obsluhaPacienta);
            response(message);
            return;
        }

        System.out.println("Pacient id=" + msg.getPacient().id()
                + " dostal sestru id=" + msg.getSestra().id());

        Pacient pacient = msg.getPacient();
        Sestra sestra = msg.getSestra();
        Ambulancia ambulancia = msg.getAmbulancia();

        pacient.setAktualnaSestra(sestra);
        sestra.setAktualnyPacient(pacient);
        sestra.setAktualnaAmbulancia(ambulancia);
        sestra.setPoloha(ambulancia);

        message.setCode(Mc.vstupneVysetreniePacienta);
        message.setAddressee(Id.agentVstupnehoVysetrenia);
        request(message);
	}

	//meta! sender="AgentAmbulancii", id="32", type="Response"
	public void processPridelenieAmbulancie(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        if (msg.getAmbulancia() == null) {
            System.out.println("Pacient caka, nie je volna ambulancia id="
                    + msg.getPacient().id());

            message.setCode(Mc.obsluhaPacienta);
            response(message);
            return;
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

            message.setCode(Mc.obsluhaPacienta);
            response(message);
        }
	}

	//meta! sender="AgentLekarov", id="51", type="Response"
	public void processPridelenieLekara(MessageForm message)
	{
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

        message.setCode(Mc.pridelenieAmbulancie);
        message.setAddressee(Id.agentAmbulancii);
        request(message);
	}

	//meta! sender="AgentVstupnehoVysetrenia", id="22", type="Response"
	public void processVstupneVysetreniePacienta(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        System.out.println("Pacient dokoncil vstupne vysetrenie id="
                + msg.getPacient().id()
                + " typ="
                + msg.getPacient().getTyp()
                + " priorita="
                + msg.getPacient().getPriorita());

        message.setCode(Mc.obsluhaPacienta);

        // uvolnenie zdrojov
        MyMessage uvolnenieAmbulancie = new MyMessage(mySim());
        uvolnenieAmbulancie.setCode(Mc.uvolnenieAmbulancie);
        uvolnenieAmbulancie.setAddressee(Id.agentAmbulancii);
        uvolnenieAmbulancie.setAmbulancia(msg.getAmbulancia());
        notice(uvolnenieAmbulancie);

        msg.getPacient().setAktualnaAmbulancia(null);
        msg.setAmbulancia(null);


        MyMessage uvolnenieSestry = new MyMessage(mySim());
        uvolnenieSestry.setCode(Mc.uvolnenieSestry);
        uvolnenieSestry.setAddressee(Id.agentSestier);
        uvolnenieSestry.setSestra(msg.getSestra());
        notice(uvolnenieSestry);

        msg.getPacient().setAktualnaSestra(null);
        msg.setSestra(null);

        msg.setFazaPacienta(MyMessage.FazaPacienta.OSETRENIE);
        nastavPovoleneAmbulanciePreOsetrenie(msg);

        message.setCode(Mc.pridelenieAmbulancie);
        message.setAddressee(Id.agentAmbulancii);
        request(message);
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
}
