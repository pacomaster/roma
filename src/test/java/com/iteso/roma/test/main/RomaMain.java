package com.iteso.roma.test.main;

public class RomaMain {
	
	private static final int N = 20;
	
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

	public static void main(String[] args) {
		for(int x = 0; x <= N; x ++){
			// System.out.println(triangularFunction(x, M, 100, 1000, true));
			System.out.println(triangularFunction(x, 10, 10, 20, false));
			// System.out.println(Math.round(triangularFunction(x, 64, 1, 100, true)));
		}

	}

}
