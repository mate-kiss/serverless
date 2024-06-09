package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
	roleName = "audit_producer-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 10)
public class AuditProducer implements RequestHandler<DynamodbEvent, Object> {
	public Object handleRequest(DynamodbEvent request, Context context) {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB database = new DynamoDB(client);
		Table table = database.getTable("cmtr-0a4e320b-Audit-test");

		for (DynamodbEvent.DynamodbStreamRecord record : request.getRecords()) {
			if (record.getEventName().equals("INSERT")) {
				Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
				Item item = new Item()
						.withPrimaryKey("id", UUID.randomUUID().toString())
						.withString("itemKey", newImage.get("key").getS())
						.withString("modificationTime", Instant.now().toString())
						.withMap("newValue", newImage);
				table.putItem(item);
			} else if (record.getEventName().equals("MODIFY")) {
				Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();
				Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
				Item item = new Item()
						.withPrimaryKey("id", UUID.randomUUID().toString())
						.withString("itemKey", newImage.get("key").getS())
						.withString("modificationTime", Instant.now().toString())
						.withString("updatedValue", "value")
						.withInt("oldValue", Integer.parseInt(oldImage.get("value").getN()))
						.withInt("newValue", Integer.parseInt(newImage.get("value").getN()));
				table.putItem(item);
			}
		}

		return null;
	}
}
