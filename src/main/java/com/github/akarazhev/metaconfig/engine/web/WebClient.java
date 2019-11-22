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
import com.github.akarazhev.metaconfig.api.Property;
import com.github.akarazhev.metaconfig.extension.Validator;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.cert.X509Certificate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static com.github.akarazhev.metaconfig.Constants.CREATE_CONSTANT_CLASS_ERROR;
import static com.github.akarazhev.metaconfig.Constants.Messages.REQUEST_SEND_ERROR;
import static com.github.akarazhev.metaconfig.Constants.Messages.WRONG_CONFIG_NAME;

/**
 * The internal implementation of the web client. The config name must be "web-client".
 * The following parameters can be set in a config property (e.g. name, value):
 * - url: the target URL;
 * - method: http method;
 * - accept: the accept header;
 * - content-type: a content type;
 * - content: a content;
 */
public final class WebClient {
    /**
     * Settings constants for the web client.
     */
    final static class Settings {

        private Settings() {
            throw new AssertionError(CREATE_CONSTANT_CLASS_ERROR);
        }

        // The configuration name
        static final String CONFIG_NAME = "web-client";
        // The URL key
        static final String URL = "url";
        // The method key
        static final String METHOD = "method";
        // The accept key
        static final String ACCEPT = "accept";
        // The content type key
        static final String CONTENT_TYPE = "content-type";
        // The content key
        static final String CONTENT = "content";
    }

    // Status code
    private int statusCode;
    // Content
    private String content;

    private WebClient(final Builder builder) {
        final Config config = builder.config;
        try {
            Optional<Property> property = config.getProperty(Settings.URL);
            if (property.isPresent()) {
                //
                setInsecure();
                // Open a connection
                final HttpsURLConnection connection = (HttpsURLConnection) new URL(property.get().getValue()).openConnection();
                property = config.getProperty(Settings.METHOD);
                if (property.isPresent()) {
                    // Set a method
                    connection.setRequestMethod(property.get().getValue());
                }
                // Set the accept header
                config.getProperty(Settings.ACCEPT).ifPresent(acceptProp ->
                        connection.setRequestProperty(Constants.ACCEPT, acceptProp.getValue()));
                // Set the content type
                config.getProperty(Settings.CONTENT_TYPE).ifPresent(contentTypeProp ->
                        connection.setRequestProperty(Constants.CONTENT_TYPE, contentTypeProp.getValue()));
                property = config.getProperty(Settings.CONTENT);
                if (property.isPresent()) {
                    // Enable the output stream
                    connection.setDoOutput(true);
                    // Write the content type
                    writeContent(connection, property.get().getValue());
                }
                // Get a response code
                statusCode = connection.getResponseCode();
                // Get a content
                if (statusCode > 299) {
                    content = readContent(connection.getErrorStream());
                } else {
                    content = readContent(connection.getInputStream());
                }
                // Close the connection
                connection.disconnect();
            }
        } catch (final Exception e) {
            throw new RuntimeException(REQUEST_SEND_ERROR, e);
        }
    }

    /**
     * Returns a status response code.
     *
     * @return a status code.
     */
    int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns a content of the response.
     *
     * @return the content.
     */
    String getContent() {
        return content;
    }

    /**
     * Returns a json content of the response.
     *
     * @return the content.
     * @throws JsonException when a web client encounters a problem.
     */
    JsonObject getJsonContent() throws JsonException {
        return (JsonObject) Jsoner.deserialize(getContent());
    }

    private String readContent(final InputStream inputStream) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            final StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }

            return content.toString();
        }
    }

    private void writeContent(final HttpsURLConnection connection, final String content) throws IOException {
        try (final OutputStream outputStream = connection.getOutputStream()) {
            final byte[] input = content.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }
    }

    private void setInsecure() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    /**
     * Wraps and builds the instance of the web client.
     */
    public final static class Builder {
        private Config config;

        /**
         * Constructs the web client based on the configuration.
         *
         * @param config a configuration of a web client.
         */
        public Builder(final Config config) {
            // Validate the config
            this.config = Validator.of(config).
                    validate(c -> Settings.CONFIG_NAME.equals(c.getName()), WRONG_CONFIG_NAME).
                    validate(c -> c.getProperty(Settings.METHOD).isPresent(), "Method is not presented.").
                    validate(c -> c.getProperty(Settings.URL).isPresent(), "URL is not presented").
                    get();
        }

        /**
         * Builds the web client with parameters.
         *
         * @return a builder of the web client.
         */
        public WebClient build() {
            return new WebClient(this);
        }
    }
}
