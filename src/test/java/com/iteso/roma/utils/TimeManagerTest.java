package com.iteso.roma.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class TimeManagerTest extends TestCase{
	
	@Test
	public void testSeconds(){
		
		int expectedValue = 10000;
		try {
			TimeManager.initialize(new File("src/test/resources/test.settings.xml"));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			fail("Exception: " + e.getMessage());
		}
		assertEquals("Seconds value not equal to " + expectedValue, expectedValue, TimeManager.getSeconds(100));
	}

}
