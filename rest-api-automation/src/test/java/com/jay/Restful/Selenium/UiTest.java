package com.jay.Restful.Selenium;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import response.GetMessageResponse;

public class UiTest {
	
	static WebDriver driver;
	
	@BeforeTest
	public void test_InitializeDriver() throws Exception{
		driver =  new FirefoxDriver();
	}
	
	//Test to search the Selenium Test Automation word in google page
	@Test()
	public void test_UI() throws Exception {
		driver.get("http://www.google.com");
		Thread.sleep(100);
		WebElement element = driver.findElement(By.name("q"));
		element.sendKeys("Selenium Test Automation");
		element.submit();
		String Title =driver.getTitle();
		Assert.assertEquals(Title, "Google");
		System.out.println("Page displayed");
	}
	
	//Test trying to login to Facebook.com and logout
	@Test()
	public void test_Facebook() throws Exception{
		driver.get("http://www.facebook.com");
		WebElement element = driver.findElement(By.id("email"));
		element.sendKeys("username");
		driver.findElement(By.id("pass")).sendKeys("Password");
		driver.findElement(By.id("loginbutton")).click();		
		wait(100);
	}
	
	
	@AfterTest()
	public void test_closeDriver(){
		driver.close();
		driver.quit();
	}

}
