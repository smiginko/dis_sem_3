package agents.agentlekarov.instantassistants;

import agents.agentlekarov.*;
import OSPABA.*;
import simulation.*;

//meta! id="84"
public class UvolniLekara extends OSPABA.Action
{
	public UvolniLekara(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void execute(MessageForm message)
	{
	}

	@Override
	public AgentLekarov myAgent()
	{
		return (AgentLekarov)super.myAgent();
	}

}
