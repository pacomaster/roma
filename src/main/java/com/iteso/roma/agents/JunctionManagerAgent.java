package com.iteso.roma.agents;

import java.util.ArrayList;

import com.iteso.roma.sumo.Phase;
import com.iteso.roma.sumo.TrafficLightState;

import jade.core.Agent;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * Class to manage the creation of agents
 * @author Francisco Amezcua
 *
 */
@SuppressWarnings("serial")
public class JunctionManagerAgent extends Agent{
	
	private ContainerController mainContainer;
	
	public JunctionManagerAgent(ContainerController mainContainer) {
		this.mainContainer = mainContainer;
	}
	
	protected void setup() {
		
		TrafficLightState t10 = new TrafficLightState("GGGrrrrrGGGrrrrr", 15);
		TrafficLightState t11 = new TrafficLightState("yyyrrrrryyyrrrrr", 4);
		
		TrafficLightState t20 = new TrafficLightState("rrrGrrrrrrrGrrrr", 6);
		TrafficLightState t21 = new TrafficLightState("rrryrrrrrrryrrrr", 4);
		
		TrafficLightState t30 = new TrafficLightState("rrrrGGGrrrrrGGGr", 15);
		TrafficLightState t31 = new TrafficLightState("rrrryyyrrrrryyyr", 4);
		
		TrafficLightState t40 = new TrafficLightState("rrrrrrrGrrrrrrrG", 6);
		TrafficLightState t41 = new TrafficLightState("rrrrrrryrrrrrrry", 4);
		
		TrafficLightState[] t1 = {t10, t11};
		TrafficLightState[] t2 = {t20, t21};
		TrafficLightState[] t3 = {t30, t31};
		TrafficLightState[] t4 = {t40, t41};
		
		Phase pha1 = new Phase(t1);
		Phase pha2 = new Phase(t2);
		Phase pha3 = new Phase(t3);
		Phase pha4 = new Phase(t4);
		
		PhaseAgent phaseAgent1 = new PhaseAgent("pha1","J5",pha1);	
		PhaseAgent phaseAgent2 = new PhaseAgent("pha2","J5",pha2);
		PhaseAgent phaseAgent3 = new PhaseAgent("pha3","J5",pha3);	
		PhaseAgent phaseAgent4 = new PhaseAgent("pha4","J5",pha4);
		
		ArrayList<PhaseAgent> phasesList = new ArrayList<PhaseAgent>();
		phasesList.add(phaseAgent1);
		phasesList.add(phaseAgent2);
		phasesList.add(phaseAgent3);
		phasesList.add(phaseAgent4);
		
		JunctionAgent junctonAgent5 = new JunctionAgent("J5", phasesList, pha1);
		
		try {			
			mainContainer.acceptNewAgent("pha1", phaseAgent1).start();
			mainContainer.acceptNewAgent("pha2", phaseAgent2).start();
			mainContainer.acceptNewAgent("pha3", phaseAgent3).start();
			mainContainer.acceptNewAgent("pha4", phaseAgent4).start();
			
			mainContainer.acceptNewAgent("J5", junctonAgent5).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
