package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<HashMap<String, Object>, Map<String, Object>> {
	public Map<String, Object> handleRequest(HashMap<String, Object> request, Context context) {
		int principalId = (int) request.get("principalId");
		Map<String, String> content =(Map<String, String>) request.get("content");

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB database = new DynamoDB(client);
		Table table = database.getTable("cmtr-0a4e320b-Events-test");
		Item item = new Item()
				.withPrimaryKey("id", UUID.randomUUID().toString())
				.withInt("principalId", principalId)
				.withString("createdAt", new Date().toString())
				.withMap("body", content);
		table.putItem(item);

		Map<String, Object> result = item.asMap();
		result.put("statusCode", 201);
		return result;
	}
}
