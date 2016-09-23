package com.iteso.roma.agents;

import java.util.ArrayList;

import com.iteso.roma.sumo.Phase;
import com.iteso.roma.sumo.TrafficLightState;
import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.sumo.SumoCom;

/**
 * Class to manage the creation of agents
 * @author Francisco Amezcua
 *
 */
public class RomaManagerAgent extends Agent{
	
	int nextCycle = 31;
	
	private ContainerController mainContainer;
	private int romaManagerId;
	
	public RomaManagerAgent(int romaManagerId, ContainerController mainContainer) {
		this.romaManagerId = romaManagerId;
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
		
		LaneAgent laneAgent10 = new LaneAgent("E1_0","J5");
		LaneAgent laneAgent11 = new LaneAgent("E1_1","J5");
		
		LaneAgent laneAgent30 = new LaneAgent("E3_0","J5");
		LaneAgent laneAgent31 = new LaneAgent("E3_1","J5");
		
		LaneAgent laneAgent50 = new LaneAgent("E5_0","J5");
		LaneAgent laneAgent51 = new LaneAgent("E5_1","J5");
	
		LaneAgent laneAgent70 = new LaneAgent("E7_0","J5");
		LaneAgent laneAgent71 = new LaneAgent("E7_1","J5");
		
		try {			
			mainContainer.acceptNewAgent("pha1", phaseAgent1).start();
			mainContainer.acceptNewAgent("pha2", phaseAgent2).start();
			mainContainer.acceptNewAgent("pha3", phaseAgent3).start();
			mainContainer.acceptNewAgent("pha4", phaseAgent4).start();
			
			mainContainer.acceptNewAgent("E1_0", laneAgent10).start();
			mainContainer.acceptNewAgent("E1_1", laneAgent11).start();
			mainContainer.acceptNewAgent("E3_0", laneAgent30).start();
			mainContainer.acceptNewAgent("E3_1", laneAgent31).start();
			mainContainer.acceptNewAgent("E5_0", laneAgent50).start();
			mainContainer.acceptNewAgent("E5_1", laneAgent51).start();
			mainContainer.acceptNewAgent("E7_0", laneAgent70).start();
			mainContainer.acceptNewAgent("E7_1", laneAgent71).start();
			
			mainContainer.acceptNewAgent("J5", junctonAgent5).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
