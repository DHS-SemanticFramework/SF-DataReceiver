package com.application.DataReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class DataReceiver {

	public static JSONArray earthquakeEventReceiver(String cityLat, String cityLong, String year, String month,
			String day, String magnitude, String source, String username, String password, String pageNumber,
			String resultsPerPage, Logger logger) {

		String date, enddate;
		if (month.equals("null")) {
			date = year + "-01-01";
			enddate = year + "-12-31";
		} else if (day.equals("null")) {
			date = year + "-" + month + "-01";
			String lastday = getLastDateOfMonth(date.replaceAll("-", "/"));
			enddate = year + "-" + month + "-" + lastday;
		} else {
			date = year + "-" + month + "-" + day;
			enddate = year + "-" + month + "-" + day;
		}

		if (Double.parseDouble(magnitude) < 5.0) {
			magnitude = "5.0";
		}

		int offset = Integer.parseInt(resultsPerPage) * (Integer.parseInt(pageNumber) - 1);
		String urlToRead;

		urlToRead = source.replace("[SelectedDate]", date).replace("[EndDate]", enddate)
				.replace("[magnitude]", magnitude).replace("[CityLat]", cityLat).replace("[CityLong]", cityLong)
				.replace("[limit]", resultsPerPage).replace("[offset]", String.valueOf(offset));
		System.out.println(urlToRead);
		StringBuilder result = new StringBuilder();
		URL url;
		try {
			url = new URL(urlToRead);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			for (String line; (line = reader.readLine()) != null;) {
				result.append(line + "\n");
			}

		} catch (IOException e) {
			logger.severe("Exception:" + e);
			e.printStackTrace();
		}
		return DataTranslator.translateEvents(result.toString());

	}

	public static JSONArray eventReceiver(String cityLat, String cityLong, String year, String month, String day,
			String source, String username, String password, String pageNumber, String resultsPerPage, Logger logger,
			String eventtype) {

		String date, enddate;
		if (month.equals("null")) {
			date = year + "-01-01";
			enddate = year + "-12-31";
		} else if (day.equals("null")) {
			date = year + "-" + month + "-01";
			String lastday = getLastDateOfMonth(date.replaceAll("-", "/"));
			enddate = year + "-" + month + "-" + lastday;
		} else {
			date = year + "-" + month + "-" + day;
			enddate = year + "-" + month + "-" + day;
		}

		int offset = Integer.parseInt(resultsPerPage) * (Integer.parseInt(pageNumber) - 1);
		String urlToRead;

		urlToRead = source.replace("[SelectedDate]", date).replace("[EndDate]", enddate).replace("[CityLat]", cityLat)
				.replace("[CityLong]", cityLong).replace("[limit]", resultsPerPage)
				.replace("[offset]", String.valueOf(offset));
		System.out.println(urlToRead);
		StringBuilder result = new StringBuilder();
		URL url;
		try {

			url = new URL(urlToRead);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestMethod("GET");

			if (!username.equals("null") && !password.equals("null")) {
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));

				String authHeaderValue = "Basic " + new String(encodedAuth);
				conn.setRequestProperty("Authorization", authHeaderValue);
			}
			if (conn.getResponseCode() == 200) {

				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				for (String line; (line = reader.readLine()) != null;) {
					result.append(line + "\n");
				}
			}
		} catch (IOException e) {
			logger.severe("Exception:" + e);
			e.printStackTrace();
		}
		if (eventtype.equals("firecall")) {
			return DataTranslator.translateFirecallEvents(result.toString());

		}
		/*
		 * Example of calling a new method i.e. for translating fire events else
		 * if(eventtype.equals("fire")) { return
		 * DataTranslator.translateFireEvents(result.toString()); }
		 */

		return DataTranslator.translateEvents(result.toString());

	}

	public static JSONArray productsReceiver(String eventDate, String eventLat, String eventLong, String source,
			String username, String password, Logger logger, String address) {

		String minus = dateCalculation(eventDate.substring(0, 10), "minus");
		String plus = dateCalculation(eventDate.substring(0, 10), "plus");
		ArrayList<String> dataSources = new ArrayList<String>();

		source = source.replace("[eventLat]", eventLong).replace("[eventLong]", eventLat)
				.replace("[eventStartTime]", minus).replace("[eventEndTime]", plus);
		System.out.println(source);
		dataSources.add(source);
		String result = "";

		JSONArray finalresult = new JSONArray();

		if (!source.contains("onda-dias")) {
			result = "";

			URL url;
			try {
				url = new URL(source);

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json");

				if (!username.equals("null") && !password.equals("null")) {
					String auth = username + ":" + password;
					byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));

					String authHeaderValue = "Basic " + new String(encodedAuth);
					conn.setRequestProperty("Authorization", authHeaderValue);
				}

				if (conn.getResponseCode() == 200) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					for (String line; (line = reader.readLine()) != null;) {

						result += line;
					}

					finalresult = DataTranslator.translateCopernicusMetadata(result.toString(), "dhus", address);
				} else {

					logger.info("[Response code from " + source + "]: " + conn.getResponseCode() + " \n");
				}

			} catch (IOException e) {

				logger.severe("Exception:" + e);
				e.printStackTrace();
			}
		} else {
			result = "";
			Unirest.setTimeouts(0, 0);
			HttpResponse<String> respon;
			try {
				respon = Unirest.get(source).header("Cookie", "SRVNAME=N01").asString();

				result += respon.getBody();

				finalresult = DataTranslator.translateCopernicusMetadata(result.toString(), "onda-dias", address);
			} catch (UnirestException e) {

				logger.severe("Exception:" + e);
				e.printStackTrace();
			}
		}

		return finalresult;

	}

	public static String dateCalculation(String date, String operation) {

		Instant inst = Instant.parse(date + "T00:00:00.00Z");

		if (operation.equals("minus")) {
			// subtract 12 Days to Instant
			Instant value = inst.minus(Period.ofDays(12));

			return value.toString().substring(0, 10);
		}
		if (operation.equals("plus")) {
			// add 12 Days to Instant
			Instant value = inst.plus(Period.ofDays(12));

			return value.toString().substring(0, 10);
		}
		return "null";
	}

	public static String getLastDateOfMonth(String date) {

		LocalDate convertedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		convertedDate = convertedDate.withDayOfMonth(convertedDate.getMonth().length(convertedDate.isLeapYear()));
		return String.valueOf(convertedDate.getDayOfMonth());
	}
}
