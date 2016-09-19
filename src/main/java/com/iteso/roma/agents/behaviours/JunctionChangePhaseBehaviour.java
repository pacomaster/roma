package com.iteso.roma.agents.behaviours;

import java.util.logging.Logger;

import com.iteso.roma.agents.JunctionAgent;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import trasmapi.sumo.SumoCom;

public class JunctionChangePhaseBehaviour extends TickerBehaviour{
	
	private static final Logger logger = Logger.getLogger(JunctionChangePhaseBehaviour.class.getName());
	
	JunctionAgent junctionAgent;
	int nextCycle;

	public JunctionChangePhaseBehaviour(Agent agent, long period, int firstCycle) {
		super(agent, period);
		this.junctionAgent = (JunctionAgent)agent;
		this.nextCycle = firstCycle;
	}

	@Override
	protected void onTick() {
		int sumoTimeFull = SumoCom.getCurrentSimStep();
		int sumoTime = sumoTimeFull / 1000;
		
		// This ticker changes the current configuration of the traffic light after completes its time
		if(sumoTime > nextCycle){					
			changePhase(myAgent);					
			logger.info(sumoTime + " " + junctionAgent.getJunctionId() + "-" + junctionAgent.getPhasesList().get(0).getPhaseId() + " P: " + junctionAgent.getPhaseStep() + " nextCycle: " + junctionAgent.getPhaseTimes()[junctionAgent.getPhaseStep()]);
		}
	}
	
	/**
	 * Changes to the next element in the array in the current phase.
	 * @param myAgent Reference to the current agent
	 */
	public void changePhase(Agent myAgent){
		junctionAgent.setPhaseStep(junctionAgent.getPhaseStep() + 1);
		// Check for last phase
		if(junctionAgent.getPhaseStep() == junctionAgent.getPhaseTimes().length - 1){
			// Request next phase
			myAgent.addBehaviour(new JunctionRequestPhaseTimeBehaviour(junctionAgent));
		}
		// If this is the last element change to the next phase
		if(junctionAgent.getPhaseStep() == junctionAgent.getPhaseTimes().length){
			junctionAgent.setPhaseStep(0);
			junctionAgent.setPhaseValues(junctionAgent.getNextPhaseValues());
			junctionAgent.setPhaseTimes(junctionAgent.getNextPhaseTimes());
			junctionAgent.getPhasesList().add(junctionAgent.getPhasesList().get(0));
			junctionAgent.getPhasesList().remove(0);
		}
		// Changes the phase in SUMO
		junctionAgent.getSumoTrafficLight().setState(junctionAgent.getPhaseValues()[junctionAgent.getPhaseStep()]);
		nextCycle += junctionAgent.getPhaseTimes()[junctionAgent.getPhaseStep()];
	}

}
