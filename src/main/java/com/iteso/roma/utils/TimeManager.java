package com.iteso.roma.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TimeManager {
	
	private static int SECOND = 50;
	
	public static void initialize(File fXmlFile) throws SAXException, IOException, ParserConfigurationException {
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
				
		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();					
		NodeList nList = doc.getElementsByTagName("delay");
		Node nNode = nList.item(0);
		Element eElement = (Element) nNode;
		SECOND = Integer.parseInt(eElement.getAttribute("value"));
	}
	
	public static int getSeconds(int seconds){
		return seconds * SECOND;
	}
}
