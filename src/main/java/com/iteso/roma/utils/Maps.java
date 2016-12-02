package com.iteso.roma.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Maps {
	
	/*
	<nodes>
	    <node id="1" x="-250.0" y="0.0" />
	    <node id="2" x="+250.0" y="0.0" />
	    <node id="3" x="+251.0" y="0.0" />
	</nodes>
	*/
	
	public static void main(String[] args) {
		
		try {
			
			FileWriter fw = null;
			BufferedWriter bw = null;
		    PrintWriter writer = null;
		    
		    FileWriter fw2 = null;
			BufferedWriter bw2 = null;
		    PrintWriter writer2 = null;
			try{
				fw = new FileWriter(new File("romaSimulations/data/nodes.nod.xml"), true);
				fw2 = new FileWriter(new File("romaSimulations/data/edges.edg.xml"), true);
				bw = new BufferedWriter(fw);
				bw2 = new BufferedWriter(fw2);
	    	    writer = new PrintWriter(bw);
	    	    writer2 = new PrintWriter(bw2);
	    	    
	    	    writer.println("<nodes>");
	    	    writer2.println("<edges>");
	    	    
	    	    String[] coordinates = getCoordinates();
	    	    String[] coorOrigin = coordinates[0].split(",");
	    	    
	    	    double originLatitud = Double.parseDouble(coorOrigin[0]);
	    	    double originLongitude = Double.parseDouble(coorOrigin[1]);
	    	    writer.println("<node id=\"0\" x=\"0\" y=\"0\" />");
	    	    
	    	    for (int id = 1; id < coordinates.length; id++) {
	    	    	String[] coor = coordinates[id].split(",");
	    	    	double coorLatitud = Double.parseDouble(coor[0]);
		    	    double coorLongitude = Double.parseDouble(coor[1]);
		    	    
		    	    double dLat = getLatitudDistance(originLatitud, coorLongitude, coorLatitud, coorLongitude);
		    	    double dLon = getLongitudeDistance(coorLatitud, originLongitude, coorLatitud, coorLongitude);	    	    
		    	    
				    writer.println("<node id=\"" + id + "\" x=\"" + String.valueOf(dLat) + "\" y=\"" + String.valueOf(dLon) + "\" />");
				    writer2.println("<edge id=\"" + (id - 1) + "\" from=\"" + (id - 1) + "\" to=\"" + id + "\" />");
				}
	    	    
	    	    writer.println("</nodes>");
	    	    writer2.println("</edges>");
				
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				writer.close();
				bw.close();
				fw.close();
				
				writer2.close();
				bw2.close();
				fw2.close();
				
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
//		double res = measure(-103.3552712, 20.6810521, -103.3419514, 20.6810872);
//		System.out.println(res);
//		res = distFrom(-103.3552712, 20.6810521, -103.3419514, 20.6810872);
//		System.out.println(res);
		
	}
	
	private static double measure(double lat1, double lon1, double lat2, double lon2){
		// generally used geo measurement function
	    double R = 6378.137; // Radius of earth in KM
	    double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
	    double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *  Math.sin(dLon/2) * Math.sin(dLon/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double d = R * c;
	    return d * 1000; // meters
	}
	
//	private static double getLatitudDistance(double lat1, double lon1, double lat2, double lon2){
//		// generally used geo measurement function
//		double R = 6378.137; // Radius of earth in KM
//		double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
//	    double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
//	    
//	    int sign = -1;
//	    if(dLat >= 0) sign = 1;	    
//	    
//	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *  Math.sin(dLon/2) * Math.sin(dLon/2);
//	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//	    double d = R * c;
//	    return d * 1000 * sign; // meters
//	}
//	
//	private static double getLongitudeDistance(double lat1, double lon1, double lat2, double lon2){
//		// generally used geo measurement function
//		double R = 6378.137; // Radius of earth in KM
//		double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
//	    double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
//	    
//	    int sign = -1;
//	    if(dLon >= 0) sign = 1;	    
//	    
//	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *  Math.sin(dLon/2) * Math.sin(dLon/2);
//	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//	    double d = R * c;
//	    return d * 1000 * sign; // meters
//	    
//	}
	
	private static double getLatitudDistance(double lat1, double lng1, double lat2, double lng2){ 	    
	    double earthRadius = 6378.137; // miles (or 6371.0 kilometers)
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    
	    int sign = -1;
	    if(dLat >= 0) sign = 1;
	    
	    double sindLat = Math.sin(dLat / 2);
	    double sindLng = Math.sin(dLng / 2);
	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
	            * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c * sign;

	    return dist *1000;
	}
	
	private static double getLongitudeDistance(double lat1, double lng1, double lat2, double lng2){
		double earthRadius = 6378.137; // miles (or 6371.0 kilometers)
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    
	    int sign = -1;
	    if(dLng >= 0) sign = 1;
	    
	    double sindLat = Math.sin(dLat / 2);
	    double sindLng = Math.sin(dLng / 2);
	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lng1)) * Math.cos(Math.toRadians(lng2));
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c * sign;

	    return dist * 1000;
	    
	}
	
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
	    double earthRadius = 3958.75; // miles (or 6371.0 kilometers)
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double sindLat = Math.sin(dLat / 2);
	    double sindLng = Math.sin(dLng / 2);
	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
	            * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    return dist;
	    }
	
	private static String[] getCoordinates(){
		String[] str = {

				"-103.3504432,20.6780057,0.0",

				"-103.3497995,20.6780158,0.0",

				"-103.3490056,20.6780408,0.0",

				"-103.3482009,20.6780509,0.0",

				"-103.3475035,20.678076,0.0",

				"-103.3466506,20.6781086,0.0",

				"-103.3458593,20.6781086,0.0",

				"-103.3504808,20.6772403,0.0",

				"-103.3503574,20.6772629,0.0",

				"-103.348968,20.6773106,0.0",

				"-103.3497593,20.677273,0.0",

				"-103.3488178,20.6773533,0.0",

				"-103.3481553,20.6773533,0.0",

				"-103.3474821,20.6774035,0.0",

				"-103.3472809,20.677416,0.0",

				"-103.3465675,20.6773482,0.0",

				"-103.3466211,20.6774637,0.0",

				"-103.3458191,20.6773608,0.0",

				"-103.350513,20.6765201,0.0",

				"-103.3497459,20.6765352,0.0",

				"-103.3489251,20.6765502,0.0",

				"-103.3481151,20.6765653,0.0",

				"-103.3474284,20.6765804,0.0",

				"-103.3465916,20.6766105,0.0",

				"-103.3457869,20.6766406,0.0",

				"-103.3505559,20.6757673,0.0",

				"-103.3497137,20.6757924,0.0",

				"-103.3488768,20.6758024,0.0",

				"-103.3480722,20.6757723,0.0",

				"-103.3473694,20.6758376,0.0",

				"-103.3465701,20.6758526,0.0",

				"-103.3457494,20.6758827,0.0"
		};
		return str;
	}

}
