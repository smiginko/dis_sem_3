package entity;

import OSPABA.Entity;
import OSPABA.Simulation;

public class Lekar extends Entity {

    private boolean jeObsadeny;
    private Pacient aktualnyPacient;
    private Ambulancia aktualnaAmbulancia;
    private Ambulancia poloha;

    public Lekar(Simulation mySim) {
        super(mySim);
        this.jeObsadeny = false;
    }

    public boolean isJeObsadeny() {
        return jeObsadeny;
    }

    public void setJeObsadeny(boolean jeObsadeny) {
        this.jeObsadeny = jeObsadeny;
    }

    public Pacient getAktualnyPacient() {
        return aktualnyPacient;
    }

    public void setAktualnyPacient(Pacient aktualnyPacient) {
        this.aktualnyPacient = aktualnyPacient;
    }

    public Ambulancia getAktualnaAmbulancia() {
        return aktualnaAmbulancia;
    }

    public void setAktualnaAmbulancia(Ambulancia aktualnaAmbulancia) {
        this.aktualnaAmbulancia = aktualnaAmbulancia;
    }

    public Ambulancia getPoloha() {
        return poloha;
    }

    public void setPoloha(Ambulancia poloha) {
        this.poloha = poloha;
    }
}
