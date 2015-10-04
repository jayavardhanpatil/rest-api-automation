package com.jay.Restful.Selenium;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import clients.ApiTestClient;
import request.CreateMessage;
import request.CreateProfile;
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
	public void test_createMessage() throws Exception {
		CreateMessage createMessage = new CreateMessage();
		createMessage.setFirstname("HarshPatil");
		createMessage.setLastname("Patil");
		createMessage.setMessage("Hello how are you");
		//String GetMessage = createMessage(createMessage);
		ObjectMapper mapper = new ObjectMapper();
		String body = mapper.writeValueAsString(createMessage);
		 Client client = Client.create();
		  WebResource webResource = client.resource("http://localhost:8080/Rest/webapi/messages");
		  ClientResponse response = null;
		  response = (ClientResponse)webResource.type("application/json").post(ClientResponse.class, body);
		  System.out.println(response.getStatus());
		  if (response.getStatus() == 200) {
			  System.out.println("successfully HarshPatil message is posted");
		  }else
			  System.out.println("Failed to post message HarshPatil Profile created");
		}
		
	@Test()
	public void test_createprofile() throws Exception {
		CreateProfile createProfile = new CreateProfile();
		createProfile.setFirstname("HarshPatil");
		createProfile.setLastname("Patil");
		createProfile.setProfilename("profileName1");
		//String GetMessage = createMessage(createMessage);
		ObjectMapper mapper = new ObjectMapper();
		String body = mapper.writeValueAsString(createProfile);
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
		
	//create profile using apache HttpClient
	@Test()
	public void CreateNewProfile() throws Exception{
		CreateProfile createProfile = new CreateProfile("name", "profilename","lastname");
		ObjectMapper mapper = new ObjectMapper();
		String body = mapper.writeValueAsString(createProfile);
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("http://localhost:8080/Rest/webapi/profiles");
		post.addHeader("Content-Type", "application/json");
		StringEntity entity = new StringEntity(body);
		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		String responcebody = response.getEntity().toString();
		System.out.println(responcebody);
		System.out.println(response.getStatusLine().getStatusCode());
	}
	//Delete particular profile with respect to id
	@Test()
	public void DeleteProfile() throws Exception{
		Client client = Client.create();
		WebResource webResource = client.resource("http://localhost:8080/Rest/webapi/profiles/{id}");
		ClientResponse response = null;
		response = webResource.delete(ClientResponse.class);
		System.out.println(response.getStatus());
	}
}
