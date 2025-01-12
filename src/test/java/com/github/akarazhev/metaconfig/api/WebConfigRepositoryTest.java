/* Copyright 2019-2021 Andrey Karazhev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package com.github.akarazhev.metaconfig.api;

import com.github.akarazhev.metaconfig.UnitTest;
import com.github.akarazhev.metaconfig.engine.web.WebServer;
import com.github.akarazhev.metaconfig.engine.web.WebServers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.akarazhev.metaconfig.engine.web.WebClient.Settings.ACCEPT_ALL_HOSTS;
import static com.github.akarazhev.metaconfig.engine.web.WebClient.Settings.CONFIG_NAME;
import static com.github.akarazhev.metaconfig.engine.web.WebClient.Settings.URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Web config repository test")
final class WebConfigRepositoryTest extends UnitTest {
    private static WebServer webServer;
    private static ConfigRepository configRepository;

    @BeforeAll
    static void beforeAll() throws Exception {
        webServer = WebServers.newTestServer().start();
        if (configRepository == null) {
            final var properties = new ArrayList<Property>(2);
            properties.add(new Property.Builder(URL, "https://localhost:8000/api/metacfg").build());
            properties.add(new Property.Builder(ACCEPT_ALL_HOSTS, true).build());

            configRepository =
                    new WebConfigRepository.Builder(new Config.Builder(CONFIG_NAME, properties).build()).build();
        }
    }

    @AfterAll
    static void afterAll() {
        configRepository = null;

        if (webServer != null) {
            webServer.stop();
            webServer = null;
        }
    }

    @BeforeEach
    void beforeEach() {
        configRepository.saveAndFlush(Stream.of(getConfigWithSubProperties(FIRST_CONFIG),
                getConfigWithProperties(SECOND_CONFIG)));
    }

    @AfterEach
    void afterEach() {
        configRepository.delete(Stream.of(FIRST_CONFIG, SECOND_CONFIG, NEW_CONFIG));
    }

    @Test
    @DisplayName("Find configs by empty names")
    void findByEmptyNames() {
        // Check test results
        assertEquals(0, configRepository.findByNames(Stream.empty()).count());
    }

    @Test
    @DisplayName("Find configs by the not existed name")
    void findByNotExistedName() {
        // Check test results
        assertEquals(0, configRepository.findByNames(Stream.of(NEW_CONFIG)).count());
    }

    @Test
    @DisplayName("Find configs by names")
    void findConfigsByNames() {
        final var configs =
                configRepository.findByNames(Stream.of(FIRST_CONFIG, SECOND_CONFIG)).toArray(Config[]::new);
        // Check test results
        assertEquals(2, configs.length);
        final var firstExpected = getConfigWithSubProperties(FIRST_CONFIG);
        final var secondExpected = getConfigWithSubProperties(SECOND_CONFIG);
        assertEqualsConfig(firstExpected, configs[0]);
        assertEqualsProperty(firstExpected, configs[0]);
        assertEqualsConfig(secondExpected, configs[1]);
        assertEqualsProperty(secondExpected, configs[1]);
    }

    @Test
    @DisplayName("Find configs by names with the stopped web server")
    void findByNamesWithStoppedWebServer() throws Exception {
        webServer.stop();
        // Check test results
        assertThrows(RuntimeException.class, () -> configRepository.findByNames(Stream.of(FIRST_CONFIG, SECOND_CONFIG)));
        webServer = WebServers.newTestServer().start();
    }

    @Test
    @DisplayName("Find config names")
    void findNames() {
        final var names = configRepository.findNames().toArray(String[]::new);
        // Check test results
        assertEquals(2, names.length);
        assertEquals(FIRST_CONFIG, names[0]);
        assertEquals(SECOND_CONFIG, names[1]);
    }

    @Test
    @DisplayName("Find config names with the stopped web server")
    void findNamesWithStoppedWebServer() throws Exception {
        webServer.stop();
        // Check test results
        assertThrows(RuntimeException.class, () -> configRepository.findNames());
        webServer = WebServers.newTestServer().start();
    }

    @Test
    @DisplayName("Find config names by a page request")
    void findByPageRequest() {
        final var page = configRepository.findByPageRequest(new PageRequest.Builder(CONFIG).build());
        // Check test results
        assertEquals(0, page.getPage());
        assertEquals(2, page.getTotal());
        final var names = page.getNames().toArray(String[]::new);
        assertEquals(2, names.length);
        assertEquals(FIRST_CONFIG, names[0]);
        assertEquals(SECOND_CONFIG, names[1]);
    }

    @Test
    @DisplayName("Find config names by a page request")
    void findByPageRequestAndAttributes() {
        final var request = new PageRequest.Builder(CONFIG).
                attributes(Collections.singletonMap("key", "value")).
                build();
        final var page = configRepository.findByPageRequest(request);
        // Check test results
        assertEquals(0, page.getPage());
        assertEquals(2, page.getTotal());
        final var names = page.getNames().toArray(String[]::new);
        assertEquals(2, names.length);
        assertEquals(FIRST_CONFIG, names[0]);
        assertEquals(SECOND_CONFIG, names[1]);
    }

    @Test
    @DisplayName("Find config names by a name, page, size and sorting")
    void findByNameAndPageAndSizeAndSorting() {
        final var request = new PageRequest.Builder(CONFIG).
                page(1).
                size(1).
                attribute("key", "value").
                ascending(false).
                build();
        final var page = configRepository.findByPageRequest(request);
        // Check test results
        assertEquals(1, page.getPage());
        assertEquals(2, page.getTotal());
        final var names = page.getNames().toArray(String[]::new);
        assertEquals(1, names.length);
        assertEquals(FIRST_CONFIG, names[0]);
    }

    @Test
    @DisplayName("Find config names by a wrong name")
    void findByWrongName() {
        final var page = configRepository.findByPageRequest(new PageRequest.Builder(NEW_CONFIG).build());
        // Check test results
        assertEquals(0, page.getPage());
        assertEquals(0, page.getTotal());
        assertEquals(0, page.getNames().count());
    }

    @Test
    @DisplayName("Find config names by a page request with the stopped web server")
    void findByNameWithStoppedWebServer() throws Exception {
        webServer.stop();
        // Check test results
        assertThrows(RuntimeException.class,
                () -> configRepository.findByPageRequest(new PageRequest.Builder(CONFIG).build()));
        webServer = WebServers.newTestServer().start();
    }

    @Test
    @DisplayName("Save and flush a new config")
    void saveAndFlushNewConfig() {
        final var newConfig =
                configRepository.saveAndFlush(Stream.of(getConfigWithProperties(NEW_CONFIG))).findFirst();
        // Check test results
        assertTrue(newConfig.isPresent());
        assertTrue(newConfig.get().getId() > 0);
    }

    @Test
    @DisplayName("Save and flush an empty")
    void saveAndFlushEmptyConfig() {
        // Check test results
        assertEquals(0, configRepository.saveAndFlush(Stream.empty()).count());
    }

    @Test
    @DisplayName("Save and flush by the config id")
    void saveAndFlushConfigById() {
        final var firstConfig = configRepository.findByNames(Stream.of(FIRST_CONFIG)).findFirst();
        // Check test results
        assertTrue(firstConfig.isPresent());
        final var newConfig = new Config.Builder(NEW_CONFIG, Collections.emptyList()).
                id(firstConfig.get().getId()).
                build();
        Optional<Config> updatedConfig = configRepository.saveAndFlush(Stream.of(newConfig)).findFirst();
        assertTrue(updatedConfig.isPresent());
        assertTrue(updatedConfig.get().getId() > 0);
    }

    @Test
    @DisplayName("Optimistic locking error")
    void optimisticLockingError() {
        final var firstConfig = configRepository.findByNames(Stream.of(FIRST_CONFIG)).findFirst();
        assertTrue(firstConfig.isPresent());
        final var newConfig = new Config.Builder(firstConfig.get()).build();
        configRepository.saveAndFlush(Stream.of(newConfig));
        assertThrows(RuntimeException.class, () -> configRepository.saveAndFlush(Stream.of(newConfig)));
    }

    @Test
    @DisplayName("Delete configs by empty names")
    void deleteByEmptyNames() {
        // Check test results
        assertEquals(0, configRepository.delete(Stream.empty()));
    }

    @Test
    @DisplayName("Delete configs by the not existed name")
    void deleteByNotExistedName() {
        // Check test results
        assertEquals(0, configRepository.delete(Stream.of(NEW_CONFIG)));
    }

    @Test
    @DisplayName("Delete configs by names")
    void deleteByNames() {
        // Check test results
        assertEquals(2, configRepository.delete(Stream.of(FIRST_CONFIG, SECOND_CONFIG)));
    }
}
