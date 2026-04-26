package agents.agentsestier.instantassistants;

import OSPABA.*;
import agents.agentsestier.*;
import simulation.*;

//meta! id="95"
public class VyberSestru extends OSPABA.Query
{
	public VyberSestru(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void execute(MessageForm message)
	{
        MyMessage msg = (MyMessage) message;
        msg.setSestra(myAgent().vyberVolnuSestru());
	}

	@Override
	public AgentSestier myAgent()
	{
		return (AgentSestier)super.myAgent();
	}

}
