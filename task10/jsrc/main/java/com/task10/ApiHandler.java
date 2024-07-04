package com.task10;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import org.json.JSONObject;

import java.util.*;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private AWSCognitoIdentityProvider cognito = new AWSCognitoIdentityProviderClient();
	AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
	private DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		APIGatewayProxyResponseEvent response = null;
		String path = request.getPath();
		switch (path) {
			case "/signup":
				if (request.getHttpMethod().equals("POST")) {
					response = signupPost(request);
				}
				break;
			case "/signin":
				if (request.getHttpMethod().equals("POST")) {
					response = signinPost(request);
				}
				break;
			case "/tables":
				if (request.getHttpMethod().equals("GET")) {
					response = tablesGet(request);
				} else if (request.getHttpMethod().equals("POST")) {
					response = tablesPost(request);
				}
				break;
			case "/reservations":
				if (request.getHttpMethod().equals("GET")) {
					response = reservationsGet(request);
				} else if (request.getHttpMethod().equals("POST")) {
					response = reservationsPost(request);
				}
				break;
			default:
				if (path.startsWith("/tables/") && request.getHttpMethod().equals("GET")) {
					response = tableGetId(request);
				}
		}

		return response;
	}

	public APIGatewayProxyResponseEvent signupPost(APIGatewayProxyRequestEvent request) {
		JSONObject requestBody = new JSONObject(request.getBody());
		String firstName = requestBody.getString("firstName");
		String lastName = requestBody.getString("lastName");
		String email = requestBody.getString("email");
		String password = requestBody.getString("password");

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			cognito.adminCreateUser(new AdminCreateUserRequest()
					.withUserPoolId(UUID.randomUUID().toString())
					.withUsername(email)
					.withTemporaryPassword(password)
					.withUserAttributes(
							new AttributeType().withName("firstName").withValue(firstName),
							new AttributeType().withName("lastName").withValue(lastName)
					)
			);
			response.setStatusCode(200);
		} catch (Exception e) {
			response.setStatusCode(400);
		}

		return response;
	}

	public APIGatewayProxyResponseEvent signinPost(APIGatewayProxyRequestEvent request) {
		JSONObject requestBody = new JSONObject(request.getBody());
		String email = requestBody.getString("email");
		String password = requestBody.getString("password");

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			Map<String, String> authParameters = new HashMap<>();
			authParameters.put("USERNAME", email);
			authParameters.put("PASSWORD", password);
			AdminInitiateAuthResult result = cognito.adminInitiateAuth(new AdminInitiateAuthRequest()
					.withAuthParameters(authParameters)
			);
			String accessToken = result.getAuthenticationResult().getAccessToken();
			Map<String, String> responseBody = new HashMap<>();
			responseBody.put("accessToken", accessToken);
			response.setBody(new JSONObject(responseBody).toString());
			response.setStatusCode(200);
		} catch (Exception e) {
			response.setStatusCode(400);
		}

		return response;
	}

	public APIGatewayProxyResponseEvent tablesGet(APIGatewayProxyRequestEvent request) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			ScanRequest scanRequest = new ScanRequest().withTableName("cmtr-0a4e320b-Tables-test");
			ScanResult scanResult = amazonDynamoDB.scan(scanRequest);

			List<Map<String, Object>> responseBody = new ArrayList<>();
			for (Map<String, AttributeValue> item : scanResult.getItems()) {
				responseBody.add(createTablesItemMap(item));
			}
			response.setBody(new JSONObject(Map.of("tables", responseBody)).toString());
			response.setStatusCode(200);
		} catch (Exception e) {
			response.setStatusCode(400);
		}

		return response;
	}

	public APIGatewayProxyResponseEvent tablesPost(APIGatewayProxyRequestEvent request) {
		JSONObject requestBody = new JSONObject(request.getBody());
		int id = requestBody.getInt("id");
		int number = requestBody.getInt("number");
		int places = requestBody.getInt("places");
		boolean isVip = requestBody.getBoolean("isVip");
		int minOrder;
		try {
			minOrder = requestBody.getInt("minOrder");
		} catch (Exception e) {
			minOrder = -1;
		}

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			Table tables = dynamoDB.getTable("cmtr-0a4e320b-Tables-test");
			Item item = new Item()
					.withInt("id", id)
					.withInt("number", number)
					.withInt("places", places)
					.withBoolean("isVip", isVip);
			if (minOrder != -1) {
				item.withInt("minOrder", minOrder);
			}
			tables.putItem(item);
			response.setBody(new JSONObject(Map.of("id", id)).toString());
			response.setStatusCode(200);
		} catch	(Exception e) {
			response.setStatusCode(400);
		}

		return response;
	}

	public APIGatewayProxyResponseEvent tableGetId(APIGatewayProxyRequestEvent request) {
		int id = Integer.parseInt(request.getPathParameters().get("tableId"));

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			Table tables = dynamoDB.getTable("cmtr-0a4e320b-Tables-test");
			Item item = tables.getItem(new KeyAttribute("id", id));
			response.setBody(item.toJSON());
			response.setStatusCode(200);
		} catch (Exception e) {
			response.setStatusCode(400);
		}

		return response;
	}

	public APIGatewayProxyResponseEvent reservationsPost(APIGatewayProxyRequestEvent request) {
		JSONObject requestBody = new JSONObject(request.getBody());
		int id = new Random().nextInt();
		int number = requestBody.getInt("tableNumber");
		String clientName = requestBody.getString("clientName");
		String phoneNumber = requestBody.getString("phoneNumber");
		String date = requestBody.getString("date");
		String slotTimeStart = requestBody.getString("slotTimeStart");
		String slotTimeEnd = requestBody.getString("slotTimeEnd");

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			Table tables = dynamoDB.getTable("cmtr-0a4e320b-Reservations-test");
			tables.putItem(new Item()
					.withInt("id", id)
					.withInt("number", number)
					.withString("clientName", clientName)
					.withString("phoneNumber", phoneNumber)
					.withString("date", date)
					.withString("slotTimeStart", slotTimeStart)
					.withString("slotTimeEnd", slotTimeEnd)
			);
			response.setBody(new JSONObject(Map.of("reservationId", id)).toString());
		} catch (Exception e) {
			response.setStatusCode(400);
		}

		return response;
	}

	public APIGatewayProxyResponseEvent reservationsGet(APIGatewayProxyRequestEvent request) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			ScanRequest scanRequest = new ScanRequest().withTableName("cmtr-0a4e320b-Reservations-test");
			ScanResult scanResult = amazonDynamoDB.scan(scanRequest);

			List<Map<String, Object>> responseBody = new ArrayList<>();
			for (Map<String, AttributeValue> item : scanResult.getItems()) {
				responseBody.add(createReservationsItemMap(item));
			}
			response.setBody(new JSONObject(Map.of("reservations", responseBody)).toString());
			response.setStatusCode(200);
		} catch (Exception e) {
			response.setStatusCode(400);
		}

		return response;
	}

	/* -------------------- */

	private Map<String, Object> createTablesItemMap(Map<String, AttributeValue> item) {
		Map<String, Object> itemMap =  new HashMap<>();
		itemMap.put("id", Integer.valueOf(item.get("id").getN()));
		itemMap.put("number", Integer.valueOf(item.get("number").getN()));
		itemMap.put("places", Integer.valueOf(item.get("places").getN()));
		itemMap.put("isVip", item.get("isVip").getBOOL());
		AttributeValue minOrder = item.get("minOrder");
		if (minOrder != null) {
			itemMap.put("minOrder", Integer.valueOf(minOrder.getN()));
		}
		return itemMap;
	}

	private Map<String, Object> createReservationsItemMap(Map<String, AttributeValue> item) {
		Map<String, Object> itemMap =  new HashMap<>();
		itemMap.put("tableNumber", Integer.valueOf(item.get("tableNumber").getN()));
		itemMap.put("clientName", item.get("clientName").getS());
		itemMap.put("phoneNumber", item.get("phoneNumber").getS());
		itemMap.put("date", item.get("date").getS());
		itemMap.put("slotTimeStart", item.get("slotTimeStart").getS());
		itemMap.put("slotTimeEnd", item.get("slotTimeEnd").getS());
		return itemMap;
	}
}
