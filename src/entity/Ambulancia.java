package entity;

import OSPABA.Entity;
import OSPABA.Simulation;

public class Ambulancia extends Entity {

    public enum TypeAmbulancia {
        TYP_A,
        TYP_B,
    }

    private TypeAmbulancia typ;
    private boolean jeObsadena;

    public Ambulancia(Simulation mySim, TypeAmbulancia typ) {
        super(mySim);
        this.typ = typ;
        this.jeObsadena = false;
    }

    public TypeAmbulancia getTyp() {
        return typ;
    }

    public void setTyp(TypeAmbulancia typ) {
        this.typ = typ;
    }

    public boolean isJeObsadena() {
        return jeObsadena;
    }

    public void setJeObsadena(boolean jeObsadena) {
        this.jeObsadena = jeObsadena;
    }
}
