package com.jay.Restful.Selenium;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import response.GetMessageResponse;

public class UiTest {
	
	static WebDriver driver;
	
	@BeforeTest
	public void test_InitializeDriver(){
		driver =  new FirefoxDriver();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test()
	public void test_UI() throws Exception {
	
		driver.get("http://www.google.com");
		Thread.sleep(30000);
		System.out.println("Page displayed");
		driver.close();
	}

}
