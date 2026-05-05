package simulation;

import OSPABA.*;
import entity.Ambulancia;
import entity.Lekar;
import entity.Pacient;
import entity.Sestra;

import java.util.Comparator;

public class MyMessage extends OSPABA.MessageForm
{
    public enum FazaPacienta {
        VSTUPNE_VYSETRENIE,
        OSETRENIE
    }

    private Pacient pacient;

    private Lekar lekar;
    private Sestra sestra;
    private Ambulancia ambulancia;

    private boolean povolenaAmbulanciaA;
    private boolean povolenaAmbulanciaB;

    private FazaPacienta fazaPacienta;

    private double casVstupuDoAktualnehoRadu;

	public MyMessage(Simulation mySim)
	{
		super(mySim);
	}

	public MyMessage(MyMessage original)
	{
		super(original);
		// copy() is called in superclass
	}

	@Override
	public MessageForm createCopy()
	{
		return new MyMessage(this);
	}

	@Override
	protected void copy(MessageForm message)
	{
		super.copy(message);
		MyMessage original = (MyMessage)message;
		// Copy attributes
        this.pacient = original.pacient;
        this.lekar = original.lekar;
        this.ambulancia = original.ambulancia;
        this.sestra = original.sestra;
        this.povolenaAmbulanciaA = original.povolenaAmbulanciaA;
        this.povolenaAmbulanciaB = original.povolenaAmbulanciaB;
        this.fazaPacienta = original.fazaPacienta;
        this.casVstupuDoAktualnehoRadu =  original.casVstupuDoAktualnehoRadu;
	}

    public Pacient getPacient() {
        return pacient;
    }

    public void setPacient(Pacient pacient) {
        this.pacient = pacient;
    }

    public Lekar getLekar() {
        return lekar;
    }

    public void setLekar(Lekar lekar) {
        this.lekar = lekar;
    }

    public Sestra getSestra() {
        return sestra;
    }

    public void setSestra(Sestra sestra) {
        this.sestra = sestra;
    }

    public Ambulancia getAmbulancia() {
        return ambulancia;
    }

    public void setAmbulancia(Ambulancia ambulancia) {
        this.ambulancia = ambulancia;
    }

    public boolean isPovolenaAmbulanciaA() {
        return povolenaAmbulanciaA;
    }

    public void setPovolenaAmbulanciaA(boolean povolenaAmbulanciaA) {
        this.povolenaAmbulanciaA = povolenaAmbulanciaA;
    }

    public boolean isPovolenaAmbulanciaB() {
        return povolenaAmbulanciaB;
    }

    public void setPovolenaAmbulanciaB(boolean povolenaAmbulanciaB) {
        this.povolenaAmbulanciaB = povolenaAmbulanciaB;
    }

    public FazaPacienta getFazaPacienta() {
        return fazaPacienta;
    }

    public void setFazaPacienta(FazaPacienta fazaPacienta) {
        this.fazaPacienta = fazaPacienta;
    }

    public double getCasVstupuDoAktualnehoRadu() { return casVstupuDoAktualnehoRadu; }
    public void setCasVstupuDoAktualnehoRadu(double cas) { this.casVstupuDoAktualnehoRadu = cas; }

    public static final Comparator<MyMessage> PORADIE =
                 Comparator
                         .comparingInt((MyMessage msg) -> msg.getPacient().getPriorita())
                         .thenComparingDouble(msg -> msg.getPacient().getCasPrichodu())
                         .thenComparingInt(msg -> msg.getPacient().id());
}