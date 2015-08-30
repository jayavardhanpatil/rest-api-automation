package com.jay.Restful.Selenium;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import clients.ApiTestClient;
import request.CreateMessage;
import response.GetMessageResponse;


//@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiTests extends ApiTestClient {
	
	String url = "http://localhost:8080/Rest/webapi/messages";
	
	@Test()
	public void test_Get_Messages() throws Exception {

		String responseBody = getMessages();
		List<GetMessageResponse> getMessageResponse = new ObjectMapper().readValue(responseBody,new TypeReference<List<GetMessageResponse>>() {});
		Assert.assertEquals(getMessageResponse.get(0).getId(), 1);
		Assert.assertEquals(getMessageResponse.size(), 2);
	}
	 	
	@Test()
	public void test_GetMessage() throws Exception {
		String GetMessage = getMessageForGivenId(2);
		GetMessageResponse getMessageResponse2 = new ObjectMapper().readValue(GetMessage, GetMessageResponse.class);
		Assert.assertEquals(getMessageResponse2.getMessage(), "Hello India");	
	}

	@Test()
	public void test_createmessage() throws Exception {
		CreateMessage createMessage = new CreateMessage();
		createMessage.setFirstname("HarshPatil");
		createMessage.setLastname("Patil");
		createMessage.setProfilename("profileName1");
		//String GetMessage = createMessage(createMessage);
		
		ObjectMapper mapper = new ObjectMapper();
		String body = mapper.writeValueAsString(createMessage);
		
		
		 Client client = Client.create();
	  WebResource webResource = client.resource("http://localhost:8080/Rest/webapi/profiles");
	  ClientResponse response = null;
	  response = (ClientResponse)webResource.type("application/json").post(ClientResponse.class, body);
	  System.out.println(response.getStatus());
	  if (response.getStatus() == 200) {
		  System.out.println("successfully HarshPatil Profile created");
	  }else
		  System.out.println("Failed HarshPatil Profile created");
	}
	
	
}
