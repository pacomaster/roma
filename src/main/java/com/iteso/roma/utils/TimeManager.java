package com.iteso.roma.utils;

public class TimeManager {
	
	private static long timeLapsed = 0;
	private static int SECOND = 50;
	
	public static int getSeconds(int seconds){
		return seconds * SECOND;
	}
	
	public static long getTimeLapsed() {
		return timeLapsed;
	}
	
	public static void incTimeLapsed() {
		TimeManager.timeLapsed++;
	}

}
