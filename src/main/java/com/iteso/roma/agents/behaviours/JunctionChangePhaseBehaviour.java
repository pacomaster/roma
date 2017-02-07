package com.iteso.roma.agents.behaviours;

import java.util.List;
import java.util.logging.Logger;

import com.iteso.roma.agents.JunctionAgent;
import com.iteso.roma.agents.PhaseAgent;
import com.iteso.roma.jade.ConversationIds;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import trasmapi.sumo.SumoCom;

@SuppressWarnings("serial")
public class JunctionChangePhaseBehaviour extends TickerBehaviour{
	
	private static final Logger _logger = Logger.getLogger(JunctionChangePhaseBehaviour.class.getName());
	
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
			//logCurrentTrafficLightState(sumoTime);			
		}
	}

	public void changeState(){		
		if(junctionAgent.getCurrentPhase().nextState() == 0){
			changeNextPhase();
		}
		junctionAgent.getSumoTrafficLight().setState(junctionAgent.getCurrentPhase().getCurrentState());
		nextCycle += junctionAgent.getCurrentPhase().getCurrentTime();
	}

	private void changeNextPhase(){
		List<PhaseAgent> phases = junctionAgent.getPhaseAgentsList();
		
//		FIXED
//		junctionAgent.setCurrentPhase(phases.get(0).getPhase());
//		junctionAgent.getPhaseAgentsList().add(junctionAgent.getPhaseAgentsList().get(0));
//		junctionAgent.getPhaseAgentsList().remove(0);
		
		
		_logger.info("###############");	
//		int maxIndex = nextMaxVeh(phases);		
		int maxIndex = nextPonderate(phases);		

		setWaitTimes(maxIndex, phases);

		junctionAgent.setCurrentPhase(phases.get(maxIndex).getPhase());
		
		logTotals();
		logWaitTimes();
		_logger.info(" ");
		_logger.info("SELECTED: " + phases.get(maxIndex).getPhaseId());

		sendVehRequest();
	}
	
	private int nextMaxVeh(List<PhaseAgent> phases){
		int maxIndex = 0;
		int maxVeh = 0;
		int i = 0;
		for(PhaseAgent phase : phases){
			if(phase.getTotalVeh() > maxVeh){
				maxVeh = phase.getTotalVeh();
				maxIndex = i;
			}
			i++;
		}
		return maxIndex;
	}
	
	private int nextPonderate(List<PhaseAgent> phases){
		int maxIndex = 0;
		double maxValue = 0;
		int i = 0;
		_logger.info("CALCULATED TIMES");
		for(PhaseAgent phase : phases){
			//double calculatedValue = phase.getTotalVeh() * Math.sqrt(phase.getWaitTime());
			double calculatedValue = (phase.getTotalVeh() * 0.8) + (phase.getWaitTime() * 0.2);
			_logger.info(phase.getPhaseId() + " - " + calculatedValue);
			if(calculatedValue > maxValue){
				maxValue = calculatedValue;
				maxIndex = i;
			}
			i++;
		}
		return maxIndex;
	}
	
	private void setWaitTimes(int maxIndex, List<PhaseAgent> phases){
		int i = 0;
		for(PhaseAgent phase : phases){
			if(i != maxIndex){
				phase.setWaitTime(phase.getWaitTime() + phases.get(maxIndex).getPhase().getTotalTime());
			}else{
				phase.setWaitTime(1);
			}
			i++;
		}
	}
	
	private void sendVehRequest() {
		int index = 0;
		for(PhaseAgent phase : junctionAgent.getPhaseAgentsList()){
			if(index > 0){
				AID receiverPhase = AIDManager.getPhaseAID(phase.getPhaseId(), junctionAgent);
				
				ACLMessage request = ACLMessageFactory.createRequestMsg(receiverPhase, junctionAgent.getJunctionId(), ConversationIds.PHASE_VALUES_VEH);
				junctionAgent.send(request);
				ACLMessageFactory.logMessage(request);
			}
			index++;
		}
	}
	
	private void logCurrentTrafficLightState(int sumoTime) {
		_logger.info(sumoTime + 
				" " + junctionAgent.getJunctionId() + 
				"-" + junctionAgent.getPhaseAgentsList().get(0).getPhaseId() + 
				" nextCycle: " + nextCycle);
	}
	
	private void logTotals(){
		_logger.info(" ");
		_logger.info("TOTALS");
		for(PhaseAgent phase : junctionAgent.getPhaseAgentsList()){
			_logger.info(phase.getPhaseId() + " - " + phase.getTotalVeh());
		}
	}
	
	private void logWaitTimes(){
		_logger.info(" ");
		_logger.info("WAIT TIMES");
		for(PhaseAgent phase : junctionAgent.getPhaseAgentsList()){
			_logger.info(phase.getPhaseId() + " - " + phase.getWaitTime());
		}
	}

}
