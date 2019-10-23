package com.github.akarazhev.metaconfig.engine.web.internal;

import com.github.akarazhev.metaconfig.api.ConfigService;
import com.sun.net.httpserver.HttpExchange;

import java.util.Collections;
import java.util.Map;

import static com.github.akarazhev.metaconfig.engine.web.internal.ConfigConstants.Method.GET;
import static com.github.akarazhev.metaconfig.engine.web.internal.StatusCodes.METHOD_NOT_ALLOWED;

final class PingController extends AbstractController {

    PingController(final ConfigService configService) {
        super(configService);
    }

    @Override
    void execute(final HttpExchange httpExchange) throws Exception {
        if (GET.equals(httpExchange.getRequestMethod())) {
            OperationResponse<Map<String, String>> operationResponse =
                    new OperationResponse<>(Collections.singletonMap("status", "ok"));
            writeResponse(httpExchange, operationResponse);
        } else {
            httpExchange.sendResponseHeaders(METHOD_NOT_ALLOWED.getCode(), -1);
        }

        httpExchange.close();
    }
}
