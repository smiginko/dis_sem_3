package agents.agentvstupnehovysetrenia.instantassistants;

import OSPABA.*;
import entity.Pacient;
import generatory.DiscreteEmpiricGenerator;
import generatory.EmpiricData;
import simulation.*;
import agents.agentvstupnehovysetrenia.*;

import java.util.ArrayList;

//meta! id="75"
public class PrideleniePriority extends OSPABA.Action
{

    private DiscreteEmpiricGenerator priorityPeso;
    private DiscreteEmpiricGenerator prioritySanitka;

	public PrideleniePriority(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        inicializeGenerators();
    }

    @Override
	public void execute(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;
        Pacient pacient = msg.getPacient();

        int priorita;

        if (pacient.getTyp() == Pacient.TypPacienta.PESO) {
            priorita = priorityPeso.nextInt();
        } else {
            priorita = prioritySanitka.nextInt();
        }

        pacient.setPriorita(priorita);

        System.out.println("Pacient id=" + pacient.id()
                + " dostal prioritu " + priorita);
    }

	@Override
	public AgentVstupnehoVysetrenia myAgent()
	{
		return (AgentVstupnehoVysetrenia)super.myAgent();
	}

    private void inicializeGenerators() {

        MySimulation mySim = (MySimulation)super.mySim();

        ArrayList<EmpiricData> empiricDataList = new ArrayList<>();
        empiricDataList.add(new EmpiricData(1,2,0.1));
        empiricDataList.add(new EmpiricData(2,3,0.2));
        empiricDataList.add(new EmpiricData(3,4,0.15));
        empiricDataList.add(new EmpiricData(4,5,0.25));
        empiricDataList.add(new EmpiricData(5,6,0.3));
        this.priorityPeso = new DiscreteEmpiricGenerator(mySim.getSeedGenerator(), empiricDataList);

        ArrayList<EmpiricData> empiricDataList1 = new ArrayList<>();
        empiricDataList1.add(new EmpiricData(1,2,0.3));
        empiricDataList1.add(new EmpiricData(2,3,0.25));
        empiricDataList1.add(new EmpiricData(3,4,0.2));
        empiricDataList1.add(new EmpiricData(4,5,0.15));
        empiricDataList1.add(new EmpiricData(5,6,0.1));
        this.prioritySanitka = new DiscreteEmpiricGenerator(mySim.getSeedGenerator(), empiricDataList1);

    }

}
