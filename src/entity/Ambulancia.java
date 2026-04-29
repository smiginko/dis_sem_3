package entity;

import OSPABA.Entity;
import OSPABA.Simulation;

public class Ambulancia extends Entity {

    public enum TypeAmbulancia {
        TYP_A,
        TYP_B,
    }

    private final int layoutIndex;

    private TypeAmbulancia typ;
    private boolean jeObsadena;

    public Ambulancia(Simulation mySim, TypeAmbulancia typ) {
        this(mySim, typ, -1);
    }

    public Ambulancia(Simulation mySim, TypeAmbulancia typ, int layoutIndex) {
        super(mySim);
        this.typ = typ;
        this.jeObsadena = false;
        this.layoutIndex = layoutIndex;
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

    public int getLayoutIndex() {
        return layoutIndex;
    }
}
