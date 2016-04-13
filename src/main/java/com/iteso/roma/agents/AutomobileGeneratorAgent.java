package com.iteso.roma.agents;

import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoVehicle;

/**
 * Class to generate automobile agents for simulation
 * @author Francisco Amezcua
 * 
 */
@SuppressWarnings("serial")
public class AutomobileGeneratorAgent extends Agent{
	
	private final int EXTRA_TIME = 64 * 1000;
	private final int ID = 1000000;
	
	private int nextCycle = 64;
	
	private int automobileGeneratorId;
	private String routeId;
	private boolean signal;
	
	private int val = 0;
	private int automobileCounter;
	private ContainerController mainContainer;
	
	public AutomobileGeneratorAgent(int automobileGeneratorId, String routeId, boolean signal, ContainerController mainContainer) {
		this.automobileGeneratorId = automobileGeneratorId;
		this.routeId = routeId;
		this.signal = signal;
		this.automobileCounter= ID * automobileGeneratorId;
		this.mainContainer = mainContainer;
	}
	
	// ARGS
	// (automobileGeneratorId, streetId,  timeToStart)
	protected void setup(){		
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){
					nextCycle += 64;
					int timeToAdd = sumoTimeFull + EXTRA_TIME;
					
					long automobilesToCreate = Math.round(triangularFunction(val, 64, 1, 32, signal)) * 1;
					val++;
					System.out.println(sumoTime + " - GEN_" + routeId + ": " + automobilesToCreate);
					
					for(int i = 0; i < automobilesToCreate; i++){
						
						int id = automobileCounter;
						byte b = 0;
						
						SumoVehicle sumoVehicle = new SumoVehicle(id, "CarA", routeId, timeToAdd, 0, b);
						SumoCom.addVehicleToSimulation(sumoVehicle);
						
						String str = "veh" + id;											
						VehicleAgent vehicleAgent = new VehicleAgent(str);
						try {
							mainContainer.acceptNewAgent(str, vehicleAgent).start();
						} catch (StaleProxyException e) {
							e.printStackTrace();
						}
						
						//System.out.println("Create AutAge" + id);
						automobileCounter++;
					}
				}
			}
		});
		
	}
	
	/**
	 * Generate a triangular function
	 * 
	 * @param x the x value of the function
	 * @param pending how many points do you want in the triangle function
	 * @param min starting value in y
	 * @param max last value in y
	 * @param asc if the function starts ascending
	 * @return
	 */
	public static double triangularFunction(double x, double pending, double min, double max, boolean asc){
		// triangular function
		// f(x) = 1 - 2 |nint((1/2)x) - ((1/2)x)|
		
		if(asc) x += (pending/2);
		return ((1 - 2 * (Math.abs(Math.round((1/pending) * x) - ((1/pending) * x)))) * (max - min)) + min;
	}
	
}
