package com.iteso.roma.utils;

public class CreateVehiclesXML {
	
	static int val = 0;
	static int automobileCounter = 0;
	
	public static void main(String[] args) {
		for(int c = 0; c < 32; c++){
			//System.out.println(Math.round(triangularFunction(c, 64, 1, 32, true)) * 1);
			
			long automobilesToCreate = Math.round(triangularFunction(c, 64, 1, 32, true)) * 1;
			for(int i = 0; i < automobilesToCreate; i++){
				
				System.out.println("<vehicle depart=\"" + c*64 + "\" id=\"veh" + automobileCounter + "\" route=\"rou1\" type=\"CarA\" />");
				automobileCounter++;
			}
			
		}
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
