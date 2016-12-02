package com.iteso.roma.agents.behaviours;

import java.util.logging.Logger;

import com.iteso.roma.agents.JunctionAgent;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import trasmapi.sumo.SumoCom;

@SuppressWarnings("serial")
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
		
		if(sumoTime > nextCycle){
			changeState();
			logCurrentTrafficLightState(sumoTime);			
		}
	}

	public void changeState(){
		if(junctionAgent.getCurrentPhase().nextState() == 0){
			changeNextPhase();
		}else{
			junctionAgent.addBehaviour(new JunctionRequestPhaseTimeBehaviour(junctionAgent));
		}
		junctionAgent.getSumoTrafficLight().setState(junctionAgent.getCurrentPhase().getCurrentState());
		nextCycle += junctionAgent.getCurrentPhase().getCurrentTime();
	}
	
	private void changeNextPhase(){
		System.out.print("OLD - ");
		logCurrentTrafficLightState(0);
		
		junctionAgent.setCurrentPhase(junctionAgent.getNextPhase());			
		junctionAgent.getPhaseAgentsList().add(junctionAgent.getPhaseAgentsList().get(0));
		junctionAgent.getPhaseAgentsList().remove(0);
		
		System.out.print("NEW - ");
		logCurrentTrafficLightState(0);
	}
	
	private void logCurrentTrafficLightState(int sumoTime) {
		logger.info(sumoTime + 
				" " + junctionAgent.getJunctionId() + 
				"-" + junctionAgent.getPhaseAgentsList().get(0).getPhaseId() + 
				" nextCycle: " + nextCycle);
	}

}
