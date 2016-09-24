package com.iteso.roma.agents;

import jade.core.Agent;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * Class to manage the creation of agents
 * @author Francisco Amezcua
 *
 */
@SuppressWarnings("serial")
public class RomaManagerAgent extends Agent{
	
	private ContainerController mainContainer;
	
	public RomaManagerAgent(ContainerController mainContainer) {
		this.mainContainer = mainContainer;
	}
	
	protected void setup() {
		
		LaneAgent laneAgent10 = new LaneAgent("E1_0","J5");
		LaneAgent laneAgent11 = new LaneAgent("E1_1","J5");
		
		LaneAgent laneAgent30 = new LaneAgent("E3_0","J5");
		LaneAgent laneAgent31 = new LaneAgent("E3_1","J5");
		
		LaneAgent laneAgent50 = new LaneAgent("E5_0","J5");
		LaneAgent laneAgent51 = new LaneAgent("E5_1","J5");
	
		LaneAgent laneAgent70 = new LaneAgent("E7_0","J5");
		LaneAgent laneAgent71 = new LaneAgent("E7_1","J5");
		
		try {			
			mainContainer.acceptNewAgent("E1_0", laneAgent10).start();
			mainContainer.acceptNewAgent("E1_1", laneAgent11).start();
			mainContainer.acceptNewAgent("E3_0", laneAgent30).start();
			mainContainer.acceptNewAgent("E3_1", laneAgent31).start();
			mainContainer.acceptNewAgent("E5_0", laneAgent50).start();
			mainContainer.acceptNewAgent("E5_1", laneAgent51).start();
			mainContainer.acceptNewAgent("E7_0", laneAgent70).start();
			mainContainer.acceptNewAgent("E7_1", laneAgent71).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
