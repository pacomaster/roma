package com.iteso.roma.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class VehiclesCreator {
	
	static final String TYPE = "CarC";
	static final String VEHICLE_PREFIX = "v";
	static final Double CARS_PER_HOUR = 900.0;
	
	static int finalSimulationStep = 7200; //14400;
	static int multiplicationOfVehicles = 1;
	static int vehicleIdCounter = 1;
	static int simulationJump = 60;
	
	static int[] routeIds = {1, 3, 5, 7};
	static int[] routeCluster = {0, 0, 0, 0};
	static double[] routePercentage = {1.0, 0.5, 1.0, 0.5};

	public static void main(String[] args) throws IOException {
		
		File fout = new File("romaSimulations\\data\\rou_" + CARS_PER_HOUR.intValue() + ".xml");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		header(bw);
		
		for(int step = 0; step < finalSimulationStep; step += simulationJump){
			for(int i = 0; i < routeIds.length; i++){
				int rou = routeIds[i];
				String route = "rou" + rou;
				int cars = getCarsPerCluster(step, routeCluster[i], routePercentage[i]);
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
		
		bw.write("</routes>");
		
		bw.close();
		System.out.println("END");
	}
	
	public static int getCarsPerCluster(int step, int cluster, double percentage){
		
		switch(cluster){
			case 0: return clusterStatic(step, percentage);
			case 1: return clusterLow(step);
			case 2: return clusterMedium(step);
			case 3: return clusterHigh(step);
			default: return clusterStatic(step);
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
	
	public static int clusterStatic(int step){
		return clusterStatic(step, 1);
	}
	
	public static int clusterStatic(int step, double percentage){

		
		Double carsPerHour = CARS_PER_HOUR;
		Double carsPerHourPercentage = carsPerHour * percentage;
		Double carsPerMinute = carsPerHourPercentage / 60;
		
		int carsPerMinuteTruncate = carsPerMinute.intValue();
		return carsPerMinuteTruncate;
	}
	
	public static void header (BufferedWriter bw) throws IOException{
		bw.write("<routes>");
		bw.newLine();
		bw.write("<vType accel=\"2.6\" decel=\"4.5\" id=\"CarA\" length=\"6.0\" minGap=\"2.5\" maxSpeed=\"40.0\" sigma=\"0.5\" /><vType accel=\"2.6\" decel=\"4.5\" id=\"CarB\" length=\"5.5\" minGap=\"2.5\" maxSpeed=\"40.0\" sigma=\"0.5\" /><vType accel=\"2.6\" decel=\"4.5\" id=\"CarC\" length=\"5.0\" minGap=\"2.5\" maxSpeed=\"40.0\" sigma=\"0.5\" /><vType accel=\"2.6\" decel=\"4.5\" id=\"CarD\" length=\"4.5\" minGap=\"2.5\" maxSpeed=\"40.0\" sigma=\"0.5\" />");
		bw.newLine();
		bw.write("<route id=\"rou1\" edges=\"E1 E6\"/><route id=\"rou2\" edges=\"E1 E4\"/><route id=\"rou3\" edges=\"E3 E8\"/><route id=\"rou4\" edges=\"E3 E6\"/><route id=\"rou5\" edges=\"E5 E2\"/><route id=\"rou6\" edges=\"E5 E8\"/><route id=\"rou7\" edges=\"E7 E4\"/><route id=\"rou8\" edges=\"E7 E2\"/><route id=\"rou9\" edges=\"E1 E8\"/>	<route id=\"rou10\" edges=\"E3 E2\"/><route id=\"rou11\" edges=\"E5 E4\"/><route id=\"rou12\" edges=\"E7 E6\"/>");
		bw.newLine();
	}
	
}
