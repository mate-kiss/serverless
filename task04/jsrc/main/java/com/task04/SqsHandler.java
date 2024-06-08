package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sqs_handler",
	roleName = "sqs_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(targetQueue = "async_queue", batchSize = 1)
public class SqsHandler implements RequestHandler<SQSEvent, Map<String, Object>> {
	public Map<String, Object> handleRequest(SQSEvent request, Context context) {
		Logger logger = LogManager.getLogger(SqsHandler.class);

		SQSEvent.SQSMessage message = request.getRecords().get(0);
		logger.info(message.getBody());

		return new HashMap<>();
	}
}
