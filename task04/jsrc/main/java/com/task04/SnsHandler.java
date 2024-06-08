package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.events.SnsEvents;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

@LambdaHandler(lambdaName = "sns_handler",
	roleName = "sns_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SnsEvents(@SnsEventSource(targetTopic = "lambda_topic"))
public class SnsHandler implements RequestHandler<SNSEvent, Object> {
	public Object handleRequest(SNSEvent request, Context context) {
		CloudWatchLogsClient client = CloudWatchLogsClient.builder().build();
		for (SNSEvent.SNSRecord record : request.getRecords()) {
			client.putLogEvents(PutLogEventsRequest.builder().logEvents(
					InputLogEvent.builder().message(
							record.getSNS().getMessage()
					).build()
			).build());
		}

		return null;
	}
}
