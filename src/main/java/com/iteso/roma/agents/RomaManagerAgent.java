package com.iteso.roma.agents;

import java.util.ArrayList;

import com.iteso.roma.sumo.Phase;
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
		
		int[] pTimes1 = {15,4}; // 31,4
		String[] pValues1 = {"GGGrrrrrGGGrrrrr", "yyyrrrrryyyrrrrr"};	
		PhaseAgent phaseAgent1 = new PhaseAgent("pha1","J5",pTimes1,pValues1);
		
		int[] pTimes2 = {6,4}; // 6,4
		String[] pValues2 = {"rrrGrrrrrrrGrrrr", "rrryrrrrrrryrrrr"};	
		PhaseAgent phaseAgent2 = new PhaseAgent("pha2","J5",pTimes2,pValues2);
		
		int[] pTimes3 = {15,4}; // 31,4
		String[] pValues3 = {"rrrrGGGrrrrrGGGr", "rrrryyyrrrrryyyr"};	
		PhaseAgent phaseAgent3 = new PhaseAgent("pha3","J5",pTimes3,pValues3);
		
		int[] pTimes4 = {6,4}; // 6,4
		String[] pValues4 = {"rrrrrrrGrrrrrrrG", "rrrrrrryrrrrrrry"};	
		PhaseAgent phaseAgent4 = new PhaseAgent("pha4","J5",pTimes4,pValues4);
		
		ArrayList<PhaseAgent> phasesList = new ArrayList<PhaseAgent>();
		phasesList.add(phaseAgent1);
		phasesList.add(phaseAgent2);
		phasesList.add(phaseAgent3);
		phasesList.add(phaseAgent4);
		
		LaneAgent laneAgent10 = new LaneAgent("E1_0","J5");
		LaneAgent laneAgent11 = new LaneAgent("E1_1","J5");
		
		LaneAgent laneAgent30 = new LaneAgent("E3_0","J5");
		LaneAgent laneAgent31 = new LaneAgent("E3_1","J5");
		
		LaneAgent laneAgent50 = new LaneAgent("E5_0","J5");
		LaneAgent laneAgent51 = new LaneAgent("E5_1","J5");
	
		LaneAgent laneAgent70 = new LaneAgent("E7_0","J5");
		LaneAgent laneAgent71 = new LaneAgent("E7_1","J5");
		
		Phase firstPhase = new Phase(pTimes1, pValues1);
		Phase nextPhase = new Phase(pTimes2, pValues2);
		
		JunctionAgent junctonAgent5 = new JunctionAgent("J5", phasesList, firstPhase, nextPhase);
		
		
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
		
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){
					
				}
			}
		});
		
	}
	
	
}
