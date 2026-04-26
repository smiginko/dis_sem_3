package main;

import simulation.MySimulation;

public class SimulaciaUrgentu {
    public static void main(String[] args)
    {
        MySimulation sim = new MySimulation();
        sim.setMaxSimSpeed();
        sim.simulate(1, 3600);
    }
}
