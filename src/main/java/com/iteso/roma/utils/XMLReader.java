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

public class XMLReader {

	public static void main(String[] args) {
		try {

			File fXmlFile = new File("romaSimulations/data/trip.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
					
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();					
			NodeList nList = doc.getElementsByTagName("tripinfo");
			
			FileWriter fw = null;
			BufferedWriter bw = null;
		    PrintWriter writer = null;
			try{
				fw = new FileWriter(new File("romaSimulations/data/tripDynamic1501.csv"), true);
				bw = new BufferedWriter(fw);
	    	    writer = new PrintWriter(bw);
	    	    
	    	    writer.println("id,duration,timeLoss,departLane");
	    	    
	    	    for (int temp = 0; temp < nList.getLength(); temp++) {

					Node nNode = nList.item(temp);							
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {

						Element eElement = (Element) nNode;
				    	writer.println(eElement.getAttribute("id") + "," + eElement.getAttribute("duration") + "," + eElement.getAttribute("timeLoss") + "," + eElement.getAttribute("departLane"));

					}
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				writer.close();
				bw.close();
				fw.close();
				
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
