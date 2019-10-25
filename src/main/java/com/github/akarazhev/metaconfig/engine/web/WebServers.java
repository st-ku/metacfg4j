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
package com.github.akarazhev.metaconfig.engine.web;

import com.github.akarazhev.metaconfig.api.Config;
import com.github.akarazhev.metaconfig.api.ConfigService;
import com.github.akarazhev.metaconfig.engine.web.internal.ConfigServer;

import java.io.IOException;

/**
 * Provides factory methods to create a web server.
 */
public final class WebServers {

    private WebServers() {
        // Factory class
    }

    /**
     * Returns a default web server.
     *
     * @param configService a configuration service.
     * @return a web server.
     * @throws IOException when a web server encounters a problem.
     */
    public static WebServer newServer(final ConfigService configService) throws IOException {
        return new ConfigServer(configService);
    }

    /**
     * Returns a web server based on the configuration.
     *
     * @param config        config a configuration of a web server.
     * @param configService a configuration service.
     * @return a web server.
     * @throws IOException when a web server encounters a problem.
     */
    public static WebServer newServer(final Config config, final ConfigService configService) throws IOException {
        return new ConfigServer(config, configService);
    }
}
