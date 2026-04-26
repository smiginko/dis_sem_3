package agents.agentambulancii.instantassistants;

import OSPABA.*;
import simulation.*;
import agents.agentambulancii.*;

//meta! id="100"
public class UvolniAmbulanciu extends OSPABA.Action
{
	public UvolniAmbulanciu(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void execute(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;
        myAgent().uvolniAmbulanciu(msg.getAmbulancia());
	}

	@Override
	public AgentAmbulancii myAgent()
	{
		return (AgentAmbulancii)super.myAgent();
	}

}
