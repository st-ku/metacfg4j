# metacfg4j

[![Build Status](https://travis-ci.com/akarazhev/metacfg4j.svg?branch=master)](https://travis-ci.com/akarazhev/metacfg4j)
[![codecov.io](http://codecov.io/github/akarazhev/metacfg4j/coverage.svg?branch=master)](http://codecov.io/github/akarazhev/metacfg4j?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.akarazhev/metacfg4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.akarazhev/metacfg4j)

The `metacfg4j` project (that stands for the `meta configuration for java`) is a library that can be used as the
solution by creating a business abstraction or may extend an existed implementation to provide such software solutions
as: various configuration (application, user's and etc.), CRUD services, DSL, MVP.

## Architecture

This is a high-level abstraction based on the low-level API. It has been written without frameworks and delivered with a
fork of the Java Toolkit (JSON.simple)

This library has the implementation of a simple web-client/server, repositories, services, controllers. The web-server
provides implementation of REST methods. Data is persisted into a DB, by using any configured datasource.

The size of the library is ~120 Kb.
  
## Configuration and Usage

### Basic Configuration

Add a maven dependency into your project:

```xml

<dependency>
    <groupId>com.github.akarazhev</groupId>
    <artifactId>metacfg4j</artifactId>
    <version>2.1</version>
</dependency>
```

Note: This version is adopted for Java 15+, use please a 1.x version for Java 8.

Instantiate the meta configuration class in your project with the default configuration:
```java
public MetaConfig metaConfig() {
    return new MetaConfig.Builder().defaultConfig().build();
}
```
If you have the configured data source, you can use it:
```java
public MetaConfig metaConfig() {
    return new MetaConfig.Builder().dataSource(getDataSource()).build();
}
```
NOTE: The web-server will not be started, since it requires the related configuration.

### Advanced Configuration

You can instantiate the meta configuration with the custom configuration:

```java
public MetaConfig metaConfig(){
// Create the custom data mapping
final var dataMapping=new HashMap<String, String>();
        dataMapping.put(Constants.Mapping.CONFIGS_TABLE,"CONFIGS");
        dataMapping.put(Constants.Mapping.CONFIG_ATTRIBUTES_TABLE,"CONFIG_ATTRIBUTES");
        dataMapping.put(Constants.Mapping.PROPERTIES_TABLE,"PROPERTIES");
        dataMapping.put(Constants.Mapping.PROPERTY_ATTRIBUTES_TABLE,"PROPERTY_ATTRIBUTES");
// Set a fetch size
final var settings=new HashMap<String, String>();
        settings.put(FETCH_SIZE,100);
// Create the web server config
final var webServer=new Config.Builder(Server.Settings.CONFIG_NAME,
        Arrays.asList(
        new Property.Builder(Server.Settings.HOSTNAME,"localhost").build(),
        new Property.Builder(Server.Settings.API_PATH,"/api/metacfg/").build(),
        new Property.Builder(Constants.Endpoints.ACCEPT_CONFIG,"accept_config").build(),
        new Property.Builder(Constants.Endpoints.CONFIG_NAMES,"config_names").build(),
        new Property.Builder(Constants.Endpoints.CONFIG,"config").build(),
        new Property.Builder(Server.Settings.PORT,8000).build(),
        new Property.Builder(Server.Settings.BACKLOG,0).build(),
        new Property.Builder(Server.Settings.KEY_STORE_FILE,"./data/metacfg4j.keystore").build(),
        new Property.Builder(Server.Settings.ALIAS,"alias").build(),
        new Property.Builder(Server.Settings.STORE_PASSWORD,"password").build(),
        new Property.Builder(Server.Settings.KEY_PASSWORD,"password").build())
        ).build();
        // Create the meta configuration
        return new MetaConfig.Builder().
        webServer(webServer).
        dataSource(getDataSource()).
        dataMapping(dataMapping).
        dbSettings(settings).
        build();
        }
```

It's possible to configure the meta configuration as a client:
```java
public MetaConfig metaConfig() {
// Create the web client config
final var webClient=new Config.Builder(WebClient.Settings.CONFIG_NAME,
        Arrays.asList(
                new Property.Builder(WebClient.Settings.URL, "https://localhost:8000/api/metacfg").build(),
                new Property.Builder(Constants.Endpoints.ACCEPT_CONFIG_ENDPOINT, "accept_config").build(),
                new Property.Builder(Constants.Endpoints.CONFIG_NAMES_ENDPOINT, "config_names").build(),
                new Property.Builder(Constants.Endpoints.CONFIG_ENDPOINT, "config").build(),
        new Property.Builder(WebClient.Settings.ACCEPT_ALL_HOSTS,true).build()))
        .build();
        // Create the meta configuration
    return new MetaConfig.Builder().webClient(webClient).build();
}
```
NOTE: you need to call the close method at the end of processing.

### Certificate generation

To generate a certificate, you need open a command line and and enter:
```bash
keytool -genkeypair -keyalg RSA -alias alias -keypass password -keystore metacfg4j.keystore -storepass password
```
If you have certificate that's signed by CA (Certificate Authority), please use it.

### Settings

#### Web Server settings

The following settings are available:

 * `config-server` - the name of the configuration. <br/>
 * `hostname` - the server hostname. <br/>
 * `port` - the server port. <br/>
 * `backlog` - the server backlog. <br/>
 * `key-store-file` - the path to a certificate. <br/>
 * `alias` - the alias of a certificate. <br/>
 * `store-password` - the store password of a certificate. <br/>
 * `key-password` - the key password of a certificate. <br/>

### Java Usage

The Java API is available:

```java
/**
 * Provides service methods to create, read, update and delete operations.
 */
public interface ConfigService {
    /**
     * Updates configuration models.
     *
     * @param stream   a stream of configuration models.
     * @return a stream of updated configuration models.
     */
    Stream<Config> update(final Stream<Config> stream);

    /**
     * Returns all configuration names.
     *
     * @return a stream of configuration names.
     */
    Stream<String> getNames();

    /**
     * Returns selected configuration names by a config page request.
     *
     * @param request a configuration page request that has parameters: page, size, ascending.
     * @return a page with configuration names.
     */
    PageResponse getNames(final PageRequest request);

    /**
     * Returns all configuration models.
     *
     * @return a stream of configurations models.
     */
    Stream<Config> get();

    /**
     * Returns configuration models by names.
     *
     * @param stream a stream of names.
     * @return a stream of configurations models.
     */
    Stream<Config> get(final Stream<String> stream);

    /**
     * Removes configuration models by names.
     *
     * @param stream a stream of names.
     * @return a number of deleted models.
     */
    int remove(final Stream<String> stream);

    /**
     * Accepts a configuration model by names.
     *
     * @param stream a stream of names.
     */
    void accept(final Stream<String> stream);

    /**
     * Adds a consumer to provide an action.
     *
     * @param consumer an implementation of the consumer.
     */
    void addConsumer(final Consumer<Config> consumer);
}
```

### REST Usage

The REST API is available by the https protocol:

`POST api/metacfg/accept_config/CONFIG_NAMES_IN_BASE64` - calls the logic for configs. <br/>
`GET api/metacfg/config_names` - returns a list of config names. <br/>
`GET api/metacfg/config_names?page_request=CONFIG_NAMES_IN_BASE64` - returns a page response based on a page request that 
has a list of config names and pagination settings. <br/>
`GET api/metacfg/config?names=CONFIG_NAMES_IN_BASE64` - returns a list of configs. <br/>
`PUT api/metacfg/config` - creates or updates a config. <br/>
`DELETE api/metacfg/config?names=CONFIG_NAMES_IN_BASE64` - removes a list of configs. <br/>

## Build Requirements

&#8658; Java 15+ <br/>
&#8658; Maven 3.6+ <br/>
 
## Contribution
 
Contribution is always welcome. Please perform changes and submit pull requests into this branch.  
Please set your editor to use spaces instead of tabs, and adhere to the apparent style of the code you are editing.