package com.iteso.roma.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class VehiclesCreator {
	
	static final String TYPE = "CarC";
	static final String VEHICLE_PREFIX = "v";

	static int finalSimulationStep = 14400;
	static int multiplicationOfVehicles = 1;
	static int vehicleIdCounter = 1;
	static int simulationJump = 60;
	
	static int[] routeIds = {1,3,5,7};
	static int[] routeCluster = {3,1,3,1};
	static int[] routePercentage = {1,1,1,1};

	public static void main(String[] args) throws IOException {
		
		File fout = new File("rou.xml");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		for(int step = 0; step < finalSimulationStep; step += simulationJump){
			for(int i = 0; i < routeIds.length; i++){
				int rou = routeIds[i];
				String route = "rou" + rou;
				int cars = getCarsPerCluster(step, routeCluster[i]);
				for(int c = 0; c < cars; c++){
					bw.write("<vehicle id=\"" + VEHICLE_PREFIX + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"" + route + "\" type=\"" + TYPE + "\"/>");
					bw.newLine();
					vehicleIdCounter++;
				}				
			}
		}
		
//		for(int step = 0; step < finalSimulationStep; step+=simulationJump){
//			long automobilesToCreate = Math.round(triangularFunction(step, 3600, 1, 32, false)) * multiplicationOfVehicles;
//			// long automobilesToCreate = Math.round(RouteAgent.triangularFunction(step, 3600, 1, 32, true)) * multiplicationOfVehicles;
//			for(int i = 0; i < automobilesToCreate; i++){
//				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"rou1\" type=\"C1\"/>");
//				bw.newLine();
//				vehicleIdCounter++;
//				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"rou3\" type=\"C1\"/>");
//				bw.newLine();
//				vehicleIdCounter++;
//				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"rou5\" type=\"C1\"/>");
//				bw.newLine();
//				vehicleIdCounter++;
//				bw.write("<vehicle id=\"V" + vehicleIdCounter + "\" depart=\"" + step + "\" route=\"rou7\" type=\"C1\"/>");
//				bw.newLine();
//				vehicleIdCounter++;				
//			}
//		}
		bw.close();
	}
	
	public static int getCarsPerCluster(int step, int cluster){
		if(cluster == 1){
			return clusterLow(step);
		}else if(cluster == 2){
			return clusterMedium(step);
		}else{
			return clusterHigh(step);
		}
	}
	
	public static int clusterHigh(int step){
		return clusterHigh(step, 1);
	}
	
	public static int clusterHigh(int step, double percentage){
		int hour = 3600;
		double [] clusterValues = {1493.6484, 1635.9780, 1275.1099, 1111.1429, 1134.6044, 1212.6593, 1253.2527, 1315.3626, 1446.1648, 1688.6154, 1819.9231, 1603.6264};

		Double index = new Double(step / hour);
		int indexTruncate = index.intValue();
		
		Double carsPerHour = clusterValues[indexTruncate];
		Double carsPerHourPercentage = carsPerHour * percentage;
		Double carsPerMinute = carsPerHourPercentage / 60;
		
		int carsPerMinuteTruncate = carsPerMinute.intValue();
		return carsPerMinuteTruncate;
	}
	
	public static int clusterMedium(int step){
		return clusterMedium(step, 1);
	}
	
	public static int clusterMedium(int step, double percentage){
		int hour = 3600;
		double [] clusterValues = {614.3531,  788.0631,  594.4497,  529.3432,  546.3511,  585.7258,  604.8087,  618.1538,  674.3531,  769.1243,  824.9112,  688.6943};

		Double index = new Double(step / hour);
		int indexTruncate = index.intValue();
		
		Double carsPerHour = clusterValues[indexTruncate];
		Double carsPerHourPercentage = carsPerHour * percentage;
		Double carsPerMinute = carsPerHourPercentage / 60;
		
		int carsPerMinuteTruncate = carsPerMinute.intValue();
		return carsPerMinuteTruncate;
	}
	
	public static int clusterLow(int step){
		return clusterLow(step, 1);
	}
	
	public static int clusterLow(int step, double percentage){
		int hour = 3600;
		double [] clusterValues = {272.9348,  375.3823,  287.9945,  259.5801,  272.2497,  291.1072,  297.2961,  305.0939,  343.2586,  375.8354,  412.9978,  333.3425};

		Double index = new Double(step / hour);
		int indexTruncate = index.intValue();
		
		Double carsPerHour = clusterValues[indexTruncate];
		Double carsPerHourPercentage = carsPerHour * percentage;
		Double carsPerMinute = carsPerHourPercentage / 60;
		
		int carsPerMinuteTruncate = carsPerMinute.intValue();
		return carsPerMinuteTruncate;
	}
	
}
