package agents.agentsestier.instantassistants;

import OSPABA.*;
import agents.agentsestier.*;
import simulation.*;

//meta! id="91"
public class UvolniSestru extends OSPABA.Action
{
	public UvolniSestru(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void execute(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;
        myAgent().uvolniSestru(msg.getSestra());
	}

	@Override
	public AgentSestier myAgent()
	{
		return (AgentSestier)super.myAgent();
	}

}
