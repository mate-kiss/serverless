package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.events.RuleEvents;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.*;

@LambdaHandler(lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEvents(@RuleEventSource(targetRule = "uuid_trigger"))
public class UuidGenerator implements RequestHandler<Object, Object> {
	public Object handleRequest(Object request, Context context) {
		List<String> ids = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			ids.add(UUID.randomUUID().toString());
		}
		Map<String, List<String>> idsMap = new HashMap<>();
		idsMap.put("ids", ids);

		try {
			String json = new ObjectMapper().writeValueAsString(idsMap);

			AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
			s3.putObject(new PutObjectRequest("cmtr-0a4e320b-uuid-storage-test", Instant.now().toString(), new StringInputStream(json), new ObjectMetadata()));
		} catch (UnsupportedEncodingException | JsonProcessingException ignored) {
        }

        return null;
	}
}
