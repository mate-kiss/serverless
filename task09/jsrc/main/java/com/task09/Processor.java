package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@LambdaHandler(lambdaName = "processor",
	roleName = "processor-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig
public class Processor implements RequestHandler<Object, Map<String, Object>> {
	public Map<String, Object> handleRequest(Object request, Context context) {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB database = new DynamoDB(client);
		Table table = database.getTable("cmtr-0a4e320b-Weather-test");

        try {
            URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = reader.readLine();
			connection.disconnect();

			JSONObject object = new JSONObject(line);

			Map<String, Object> forecast = new HashMap<>();
			forecast.put("elevation", object.getInt("elevation"));
			forecast.put("generationtime_ms", object.getInt("generationtime_ms"));

			Map<Object, Object> hourly = new HashMap<>();
			List<BigDecimal> temperature2m = new ArrayList<>();
			for (Object t2m : object.getJSONObject("hourly").getJSONArray("temperature_2m").toList()) {
				temperature2m.add((BigDecimal) t2m);
			}
			hourly.put("temperature_2m", temperature2m);
			List<String> time = new ArrayList<>();
			for (Object t : object.getJSONObject("hourly").getJSONArray("time")) {
				time.add((String) t);
			}
			hourly.put("time", time);
			forecast.put("hourly", hourly);

			Map<Object, Object> hourlyUnits = new HashMap<>();
			hourlyUnits.put("temperature_2m", object.getJSONObject("hourly_units").getString("temperature_2m"));
			hourlyUnits.put("time", object.getJSONObject("hourly_units").getString("time"));
			forecast.put("hourly_units", hourlyUnits);

			forecast.put("latitude", object.getInt("latitude"));
			forecast.put("longitude", object.getInt("longitude"));
			forecast.put("timezone", object.getString("timezone"));
			forecast.put("timezone_abbreviation", object.getString("timezone_abbreviation"));
			forecast.put("utc_offset_seconds", object.getInt("utc_offset_seconds"));

			Item item = new Item()
					.withPrimaryKey("id", UUID.randomUUID().toString())
					.withMap("forecast", forecast);
			table.putItem(item);
        } catch (IOException ignored) {
        }

        return null;
	}
}
