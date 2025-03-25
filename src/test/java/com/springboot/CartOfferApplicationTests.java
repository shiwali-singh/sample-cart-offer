package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.OfferRequest;
import com.springboot.controller.SegmentResponse;
import org.assertj.core.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.springboot.controller.ApplyOfferRequest;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {


	private static final String URL_OFFER = "http://localhost:9001/api/v1/offer";
	private static final String URL_APPLY_OFFER = "http://localhost:9001/api/v1/cart/apply_offer";
	private final ObjectMapper mapper = new ObjectMapper();
	@Test
	public void checkFlatXForOneSegment() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer
	}

	private String sendPostRequest(String url, Object requestBody) throws Exception {
		String jsonRequest = mapper.writeValueAsString(requestBody);
		String response = postMethodReturn(url, jsonRequest);
		Assert.assertNotNull("Response should not be null", response);
		return response;
	}

	@Test
	public void checkFlatXForOneSegmentValue() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, segments);
		Assert.assertTrue("Offer should be added successfully", addOffer(offerRequest));

		sendPostRequest(URL_OFFER, offerRequest);

		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest();
		applyOfferRequest.setRestaurant_id(1);
		applyOfferRequest.setCart_value(200);
		applyOfferRequest.setUser_id(1);

		String response = sendPostRequest(URL_APPLY_OFFER, applyOfferRequest);
		int cartValue = mapper.readTree(response).get("cart_value").asInt();
		Assert.assertEquals("Verify the Cart value after applying discount", 190, cartValue);
	}

	@Test
	public void checkFlatXForAllSegments() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		segments.add("p2");
		segments.add("p3");
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, segments);
		sendPostRequest(URL_OFFER, offerRequest);

		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest();
		applyOfferRequest.setRestaurant_id(1);
		applyOfferRequest.setCart_value(200);
		applyOfferRequest.setUser_id(1);
		String response = sendPostRequest(URL_APPLY_OFFER, applyOfferRequest);

		int cartValue = mapper.readTree(response).get("cart_value").asInt();
		Assert.assertEquals("Verify the Cart value after applying discount", 190, cartValue);
	}

	@Test
	public void checkFlatXPercentageOffer() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");

		OfferRequest offerRequest = new OfferRequest(1, "FLATX%", 10, segments);
		sendPostRequest(URL_OFFER, offerRequest);
		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest();
		applyOfferRequest.setRestaurant_id(1);
		applyOfferRequest.setCart_value(200);
		applyOfferRequest.setUser_id(1);

		String response = sendPostRequest(URL_APPLY_OFFER, applyOfferRequest);

		int cartValue = mapper.readTree(response).get("cart_value").asInt();
		Assert.assertEquals("Verify the Cart value after applying discount", 180, cartValue);
	}

	@Test
	public void checkFlatXPercentageOfferForAllSegments() throws Exception {
		List<String> segments = new ArrayList<>();

		segments.add("p1");
		segments.add("p2");
		segments.add("p3");
		OfferRequest offerRequest = new OfferRequest(1, "FLATX%", 10, segments);
		sendPostRequest(URL_OFFER, offerRequest);

		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest();
		applyOfferRequest.setRestaurant_id(1);
		applyOfferRequest.setCart_value(200);
		applyOfferRequest.setUser_id(1);
		String response = sendPostRequest(URL_APPLY_OFFER, applyOfferRequest);

		int cartValue = mapper.readTree(response).get("cart_value").asInt();
		Assert.assertEquals("Verify the Cart value after applying discount", 180, cartValue);
	}

	@Test
	public void checkInvalidSegmentOffer() throws Exception {
		List<String> segments = new ArrayList<>();

		segments.add("p13231");
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, segments);
		String response = sendPostRequest(URL_OFFER, offerRequest);

		Assert.assertTrue("Verify the error message for invalid segment", response.contains("Segment not found"));
	}

	@Test
	public void checkInvalidRestaurantErrorMessage() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, segments);
		sendPostRequest(URL_OFFER, offerRequest);

		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest();
		applyOfferRequest.setRestaurant_id(1333333);
		applyOfferRequest.setCart_value(200);
		applyOfferRequest.setUser_id(1);		String response = sendPostRequest(URL_APPLY_OFFER, applyOfferRequest);

		Assert.assertTrue("Verify the error message for invalid restaurant", response.contains("Invalid restaurant ID"));
	}

	private static String postMethodReturn(String urlString, String requestBody) {
		StringBuilder response = new StringBuilder();

		try {
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");

			System.out.println("Request Body: " + requestBody);

			try (OutputStream os = con.getOutputStream()) {
				os.write(requestBody.getBytes());
				os.flush();
			}

			int responseCode = con.getResponseCode();
			Assert.assertEquals("Verify the status code", HttpURLConnection.HTTP_OK, responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) { // Success
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
				}
				System.out.println("Response: " + response.toString());
			} else {
				System.err.println("POST request failed with status code: " + responseCode);
			}
		} catch (Exception e) {
			System.err.println("Error in postMethodReturn: " + e.getMessage());
			e.printStackTrace();
		}

		return response.toString();
	}

	public boolean addOffer(OfferRequest offerRequest) throws Exception {
		String urlString = "http://localhost:9001/api/v1/offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();

		String POST_PARAMS = mapper.writeValueAsString(offerRequest);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("POST request did not work.");
		}
		return true;
	}
}