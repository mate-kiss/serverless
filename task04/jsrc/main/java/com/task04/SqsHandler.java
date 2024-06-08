package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@LambdaHandler(lambdaName = "sqs_handler",
	roleName = "sqs_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(targetQueue = "async_queue", batchSize = 10)
public class SqsHandler implements RequestHandler<SQSEvent, Object> {
	private static final Logger LOG = LogManager.getLogger(SqsHandler.class);

	public Object handleRequest(SQSEvent request, Context context) {
		for (SQSEvent.SQSMessage message : request.getRecords()) {
			LOG.info("SQS: " + message.getBody());
		}

		return null;
	}
}
