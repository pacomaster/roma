package com.iteso.roma.agents;

import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.ContainerController;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoTrafficLight;

public class JunctionAgent extends Agent{
	
	private String junctionId;
	private SumoTrafficLight myself;
	private String[] phaseValue;
	private int[] phaseTime;
	private int phase = 0;
	
	private int nextCycle;
	
	private ContainerController mainContainer;
	
	public JunctionAgent(String junctionId, int[] phaseTime, String[] phaseValue, ContainerController mainContainer) {
		this.junctionId = junctionId;
		this.myself = new SumoTrafficLight(junctionId);
		this.phaseTime = phaseTime;
		this.phaseValue = phaseValue;
		this.mainContainer = mainContainer;
		nextCycle = phaseTime[phase];
	}
	
	protected void setup(){	
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){
					changePhase();					
					System.out.println(sumoTime + " " + junctionId + " P: " + phase + " nextCycle: " + nextCycle);						
				}
			}
		});
	}
	
	public void changePhase(){
		phase++;
		if(phase >= phaseTime.length) phase = 0;
		myself.setState(phaseValue[phase]);
		nextCycle += phaseTime[phase];
	}

}
