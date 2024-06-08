package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

@LambdaHandler(lambdaName = "sqs_handler",
	roleName = "sqs_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(targetQueue = "async_queue", batchSize = 10)
public class SqsHandler implements RequestHandler<SQSEvent, Object> {
	public Object handleRequest(SQSEvent request, Context context) {
		CloudWatchLogsClient client = CloudWatchLogsClient.builder().build();
		for (SQSEvent.SQSMessage message : request.getRecords()) {
			client.putLogEvents(PutLogEventsRequest.builder().logEvents(
					InputLogEvent.builder().message(
							message.getBody()
					).build()
			).build());
		}

		return null;
	}
}
