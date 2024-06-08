package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.events.SnsEvents;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@LambdaHandler(lambdaName = "sns_handler",
	roleName = "sns_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SnsEvents(@SnsEventSource(targetTopic = "lambda_topic"))
public class SnsHandler implements RequestHandler<SNSEvent, Object> {
	private static final Logger LOG = LogManager.getLogger(SnsHandler.class);

	public Object handleRequest(SNSEvent request, Context context) {
		for (SNSEvent.SNSRecord record : request.getRecords()) {
			LOG.info("SNS: " + record.getSNS().getMessage());
		}

		return null;
	}
}
