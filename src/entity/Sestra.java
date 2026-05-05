package entity;

import OSPABA.Entity;
import OSPABA.Simulation;

public class Sestra extends Entity {

    private boolean jeObsadena;
    private Ambulancia aktualnaAmbulancia;
    private Ambulancia poloha;
    private Pacient aktualnyPacient;
    private double casOdKedyJeVolna;

    public Sestra(Simulation mySim) {
        super(mySim);
        this.jeObsadena = false;
        this.aktualnaAmbulancia = null;
        this.aktualnyPacient = null;
        this.casOdKedyJeVolna = mySim.currentTime();
    }

    public boolean jeObsadena() {
        return jeObsadena;
    }

    public void setJeObsadena(boolean jeObsadena) {
        this.jeObsadena = jeObsadena;
    }

    public Ambulancia getAktualnaAmbulancia() {
        return aktualnaAmbulancia;
    }

    public void setAktualnaAmbulancia(Ambulancia aktualnaAmbulancia) {
        this.aktualnaAmbulancia = aktualnaAmbulancia;
    }

    public Pacient getAktualnyPacient() {
        return aktualnyPacient;
    }

    public void setAktualnyPacient(Pacient aktualnyPacient) {
        this.aktualnyPacient = aktualnyPacient;
    }

    public Ambulancia getPoloha() { return poloha; }
    public void setPoloha(Ambulancia poloha) { this.poloha = poloha; }

    public double getCasOdKedyJeVolna() {
        return casOdKedyJeVolna;
    }

    public void setCasOdKedyJeVolna(double casOdKedyJeVolna) {
        this.casOdKedyJeVolna = casOdKedyJeVolna;
    }
}
