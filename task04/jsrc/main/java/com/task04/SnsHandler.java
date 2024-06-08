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

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sns_handler",
	roleName = "sns_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SnsEvents(@SnsEventSource(targetTopic = "lambda_topic"))
public class SnsHandler implements RequestHandler<SNSEvent, Map<String, Object>> {
	public Map<String, Object> handleRequest(SNSEvent request, Context context) {
		Logger logger = LogManager.getLogger(SnsHandler.class);

		SNSEvent.SNSRecord record = request.getRecords().getFirst();
		logger.info(record.getSNS().getMessage());

		return new HashMap<>();
	}
}
