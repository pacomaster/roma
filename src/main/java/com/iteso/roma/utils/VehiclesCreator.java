package com.iteso.roma.utils;

import com.iteso.roma.agents.RouteAgent;

public class VehiclesCreator {

	static int finalSimulationStep = 3600;
	static int multiplicationOfVehicles = 1;
	static int vehicleIdCounter = 0;
	static int simulationJump = 60;
	static String routeId = "rou5";
	
	public static void main(String[] args) {
		
		for(int step = 0; step < finalSimulationStep; step+=simulationJump){
			long automobilesToCreate = Math.round(RouteAgent.triangularFunction(step, 3600, 1, 32, false)) * multiplicationOfVehicles;
			// long automobilesToCreate = Math.round(RouteAgent.triangularFunction(step, 3600, 1, 32, true)) * multiplicationOfVehicles;
			for(int i = 0; i < automobilesToCreate; i++){
				System.out.println("<vehicle depart=\"" + step + "\" id=\"veh" + vehicleIdCounter + "\" route=\"" + routeId + "\" type=\"CarA\"/>");
				vehicleIdCounter++;
			}
		}		
	}
}
