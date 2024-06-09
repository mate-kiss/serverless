package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.events.RuleEvents;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@LambdaHandler(lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEvents(@RuleEventSource(targetRule = "uuid_trigger"))
public class UuidGenerator implements RequestHandler<Object, Object> {
	public Object handleRequest(Object request, Context context) {
		AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

		File file = new File(Instant.now().toString());
		try (FileWriter writer = new FileWriter(file)) {
			for (int i = 0; i < 10; i++) {
				writer.write(UUID.randomUUID().toString() + "\n");
			}

		} catch (IOException ignored) {}

		s3.putObject("cmtr-0a4e320b-uuid-storage-test", file.getName(), file);

		return null;
	}
}
