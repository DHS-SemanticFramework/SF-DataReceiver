
package com.application.DataReceiver;

import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Path("/{task}")
public class MyResource {

	@POST
	@Produces("text/plain")
	public Response retrieval(@PathParam("task") String task, String object) {

		Gson gson = new Gson();

		JsonElement json = gson.fromJson(object, JsonElement.class);
		JsonObject jobject = json.getAsJsonObject();

		Logger logger = Logger.getLogger("MyLog");

		if (task.equals("earthquake")) {
			// earthquake
			JSONArray translatedEvents = DataReceiver.earthquakeEventReceiver(jobject.get("cityLat").getAsString(),
					jobject.get("cityLong").getAsString(), jobject.get("year").getAsString(),
					jobject.get("month").getAsString(), jobject.get("day").getAsString(),
					jobject.get("magnitude").getAsString(), jobject.get("source").getAsString(),
					jobject.get("username").getAsString(), jobject.get("password").getAsString(),
					jobject.get("pageNumber").getAsString(), jobject.get("resultsPerPage").getAsString(), logger);
			return Response.status(200).entity(translatedEvents.toString()).build();
		} else if (task.equals("product")) {
			// products
			JSONArray jarray = DataReceiver.productsReceiver(jobject.get("eventDate").getAsString(),
					jobject.get("eventLat").getAsString(), jobject.get("eventLong").getAsString(),
					jobject.get("source").getAsString(), jobject.get("username").getAsString(),
					jobject.get("password").getAsString(), logger, jobject.get("address").getAsString());
			return Response.status(200).entity(jarray.toString()).build();
		} else if (task.contains("event-")) {
			// generic events
			String eventtype = task.replaceAll("event-", "");

			JSONArray translatedEvents = DataReceiver.eventReceiver(jobject.get("cityLat").getAsString(),
					jobject.get("cityLong").getAsString(), jobject.get("year").getAsString(),
					jobject.get("month").getAsString(), jobject.get("day").getAsString(),
					jobject.get("source").getAsString(), jobject.get("username").getAsString(),
					jobject.get("password").getAsString(), jobject.get("pageNumber").getAsString(),
					jobject.get("resultsPerPage").getAsString(), logger, eventtype);
			return Response.status(200).entity(translatedEvents.toString()).build();

		} else {
			return Response.status(200).entity("Unsupported task parameter.").build();
		}
	}
}
