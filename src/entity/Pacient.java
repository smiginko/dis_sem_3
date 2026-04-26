package entity;

import OSPABA.Entity;
import OSPABA.Simulation;

public class Pacient extends Entity {

    public enum TypPacienta {
        PESO,
        SANITKA
    }

    private TypPacienta typ;
    private double casPrichodu;
    private int priorita;
    private Ambulancia aktualnaAmbulancia;
    private Sestra aktualnaSestra;
    private Lekar aktualnyLekar;

    public Pacient(Simulation mySim, TypPacienta typ, double casPrichodu) {
        super(mySim);
        this.typ = typ;
        this.casPrichodu = casPrichodu;
        this.aktualnaAmbulancia = null;
        this.aktualnaSestra = null;
        this.aktualnyLekar = null;
    }

    public TypPacienta getTyp() {
        return typ;
    }

    public void setTyp(TypPacienta typ) {
        this.typ = typ;
    }

    public double getCasPrichodu() {
        return casPrichodu;
    }

    public void setCasPrichodu(double casPrichodu) {
        this.casPrichodu = casPrichodu;
    }

    public int getPriorita() {
        return priorita;
    }

    public void setPriorita(int priorita) {
        this.priorita = priorita;
    }

    public Ambulancia getAktualnaAmbulancia() {
        return aktualnaAmbulancia;
    }

    public void setAktualnaAmbulancia(Ambulancia aktualnaAmbulancia) {
        this.aktualnaAmbulancia = aktualnaAmbulancia;
    }

    public Sestra getAktualnaSestra() {
        return aktualnaSestra;
    }

    public void setAktualnaSestra(Sestra aktualnaSestra) {
        this.aktualnaSestra = aktualnaSestra;
    }

    public Lekar getAktualnyLekar() {
        return aktualnyLekar;
    }

    public void setAktualnyLekar(Lekar aktualnyLekar) {
        this.aktualnyLekar = aktualnyLekar;
    }
}

