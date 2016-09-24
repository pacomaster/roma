package com.iteso.roma.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
			long automobilesToCreate = Math.round(triangularFunction(step, 3600, 1, 32, false)) * multiplicationOfVehicles;
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
