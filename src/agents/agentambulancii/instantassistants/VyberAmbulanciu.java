package agents.agentambulancii.instantassistants;

import OSPABA.*;
import simulation.*;
import agents.agentambulancii.*;

//meta! id="98"
public class VyberAmbulanciu extends OSPABA.Query
{
	public VyberAmbulanciu(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void execute(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;

        msg.setAmbulancia(
                myAgent().vyberVolnuAmbulanciu(
                    msg.isPovolenaAmbulanciaA(),
                    msg.isPovolenaAmbulanciaB()
                )
        );
	}

	@Override
	public AgentAmbulancii myAgent()
	{
		return (AgentAmbulancii)super.myAgent();
	}

}
