package agents.agentlekarov.instantassistants;

import agents.agentlekarov.*;
import OSPABA.*;
import simulation.*;

//meta! id="82"
public class VyberLekara extends OSPABA.Query
{
	public VyberLekara(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void execute(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;
        msg.setLekar(myAgent().vyberVolnehoLekara());
	}

	@Override
	public AgentLekarov myAgent()
	{
		return (AgentLekarov)super.myAgent();
	}

}
