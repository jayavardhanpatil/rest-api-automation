package clients;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import org.testng.AssertJUnit;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.testng.Assert;

import request.CreateMessage;

public class ApiTestClient {

	String hostName = "http://localhost:8080";

	public String getMessages() throws Exception {

		HttpClient client = HttpClientBuilder.create().build();
		String url = hostName + "/Rest/webapi/messages";
		HttpGet getRequest = new HttpGet(url);
		getRequest.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(getRequest);
		AssertJUnit.assertEquals(response.getStatusLine().getStatusCode(), 200);
		HttpEntity httpEntity = response.getEntity();
		String responseBody = EntityUtils.toString(httpEntity);
		return responseBody;
	}

	public String getMessageForGivenId(int id) throws Exception {

		HttpClient client = HttpClientBuilder.create().build();
		String url = hostName + "/Rest/webapi/messages/" + id;
		HttpGet getRequest = new HttpGet(url);
		getRequest.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(getRequest);
		AssertJUnit.assertEquals(response.getStatusLine().getStatusCode(), 200);
		HttpEntity httpEntity = response.getEntity();
		String responseBody = EntityUtils.toString(httpEntity);
		return responseBody;
	}

	public String createMessage(CreateMessage createMessage) throws Exception {
		
		  Client client = Client.create();
		  WebResource webResource = client.resource("http://localhost:8080/Rest/webapi/profiles");
		  ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
		  System.out.println(response);
		  return "hello";
		
		
		/*HttpClient client = HttpClientBuilder.create().build();
		String url = hostName + "/Rest/webapi/profiles";
		ObjectMapper mapper = new ObjectMapper();
		HttpPost postRequest = new HttpPost(url);
		mapper.writeValueAsString(mapper.canSerialize(CreateMessage.class));
		postRequest.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(postRequest);
		AssertJUnit.assertEquals(response.getStatusLine().getStatusCode(), 200);
		HttpEntity httpEntity = response.getEntity();
		String responseBody = EntityUtils.toString(httpEntity);
		return responseBody;*/
	}

}
