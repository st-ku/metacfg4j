/* Copyright 2019 Andrey Karazhev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package com.github.akarazhev.metaconfig.engine.web.internal;

import com.github.akarazhev.metaconfig.Constants;
import com.github.akarazhev.metaconfig.api.Config;
import com.github.akarazhev.metaconfig.api.ConfigService;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.github.akarazhev.metaconfig.Constants.Messages.REQUEST_PARAM_NOT_PRESENT;
import static com.github.akarazhev.metaconfig.Constants.Messages.STRING_TO_JSON_ERROR;
import static com.github.akarazhev.metaconfig.engine.web.Constants.Method.GET;
import static com.github.akarazhev.metaconfig.engine.web.internal.StatusCodes.METHOD_NOT_ALLOWED;

/**
 * Provides a handler functionality for the GET configs method.
 */
final class ConfigsController extends AbstractController {

    private ConfigsController(final Builder builder) {
        super(builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void execute(final HttpExchange httpExchange) throws IOException {
        if (GET.equals(httpExchange.getRequestMethod())) {
            final OperationResponse response = getRequestParam(httpExchange.getRequestURI().getQuery(), "names").
                    map(param -> {
                        try {
                            final String array = new String(Base64.getDecoder().decode(param), StandardCharsets.UTF_8);
                            final JsonArray jsonArray = (JsonArray) Jsoner.deserialize(array);
                            final List<Config> configs = new ArrayList<>(jsonArray.size());
                            for (int i = 0; i < jsonArray.size(); i++) {
                                configService.get(jsonArray.getString(i)).ifPresent(configs::add);
                            }

                            return new OperationResponse.Builder<>().result(configs).build();
                        } catch (final Exception e) {
                            return new OperationResponse.Builder<>().error(STRING_TO_JSON_ERROR).build();
                        }
                    }).
                    orElseGet(() -> new OperationResponse.Builder<>().error(REQUEST_PARAM_NOT_PRESENT).build());
            writeResponse(httpExchange, response);
        } else {
            throw new MethodNotAllowedException(METHOD_NOT_ALLOWED.getCode(), Constants.Messages.METHOD_NOT_ALLOWED);
        }
    }

    /**
     * Wraps and builds the instance of the configs controller.
     */
    static class Builder extends AbstractBuilder {
        /**
         * Constructs a controller with the configuration service param.
         *
         * @param configService a configuration service.
         */
        Builder(final ConfigService configService) {
            super(configService);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        ConfigsController build() {
            return new ConfigsController(this);
        }
    }
}
