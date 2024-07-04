package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import org.example.OpenMetro;

import java.io.IOException;
import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
	layers = {"sdk_layer"}
)
@LambdaLayer(layerName = "sdk_layer",
	libraries = {"lib/openmetro-1.0-SNAPSHOT.jar"}
)
@LambdaUrlConfig
public class ApiHandler implements RequestHandler<Object, String> {

	public String handleRequest(Object request, Context context) {
        try {
            return new OpenMetro().getForecast();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
