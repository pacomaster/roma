package com.iteso.roma.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.iteso.roma.agents.RouteAgent;

public class VehiclesCreator {

	static int finalSimulationStep = 3600;
	static int multiplicationOfVehicles = 1;
	static int vehicleIdCounter = 1;
	static int simulationJump = 60;
	static String routeId = "R3";
	
	public static void main(String[] args) throws IOException {
		
		File fout = new File("rou.xml");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		for(int step = 0; step < finalSimulationStep; step+=simulationJump){
			long automobilesToCreate = Math.round(RouteAgent.triangularFunction(step, 3600, 1, 32, false)) * multiplicationOfVehicles;
			// long automobilesToCreate = Math.round(RouteAgent.triangularFunction(step, 3600, 1, 32, true)) * multiplicationOfVehicles;
			for(int i = 0; i < automobilesToCreate; i++){
				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"R1\" type=\"C1\"/>");
				bw.newLine();
				vehicleIdCounter++;
				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"R2\" type=\"C1\"/>");
				bw.newLine();
				vehicleIdCounter++;
				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"R3\" type=\"C1\"/>");
				bw.newLine();
				vehicleIdCounter++;
				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"R4\" type=\"C1\"/>");
				bw.newLine();
				vehicleIdCounter++;
				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"R5\" type=\"C1\"/>");
				bw.newLine();
				vehicleIdCounter++;
				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"R6\" type=\"C1\"/>");
				bw.newLine();
				vehicleIdCounter++;
				
			}
		}
		bw.close();
	}
}
