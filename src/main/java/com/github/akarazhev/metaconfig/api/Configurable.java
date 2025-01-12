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

import com.github.akarazhev.metaconfig.extension.ExtJsonable;
import com.github.akarazhev.metaconfig.extension.Validator;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.akarazhev.metaconfig.Constants.Messages.CREATE_HELPER_CLASS_ERROR;

/**
 * Extends the basic interface of <code>ExtJsonable</code>
 * and provides functionality for getting attributes and properties.
 *
 * @see ExtJsonable for more information.
 */
interface Configurable extends ExtJsonable {
    /**
     * Returns attributes which belong to configurations.
     *
     * @return attributes as a map.
     */
    Optional<Map<String, String>> getAttributes();

    /**
     * Returns attribute keys which belong to configurations.
     *
     * @return attribute keys as a stream.
     */
    Stream<String> getAttributeKeys();

    /**
     * Returns an attribute value by the key.
     *
     * @param key attribute key.
     * @return a value by the key.
     */
    Optional<String> getAttribute(final String key);

    /**
     * Returns properties which belong to configurations.
     *
     * @return properties as a stream.
     */
    Stream<Property> getProperties();

    /**
     * Returns a property by paths.
     *
     * @param paths property paths.
     * @return a property.
     */
    Optional<Property> getProperty(final String... paths);

    /**
     * Returns a property by paths.
     *
     * @param i      a current path.
     * @param paths  paths
     * @param source a current property stream.
     * @return a property.
     */
    static Optional<Property> getProperty(final int i, final String[] paths, final Stream<Property> source) {
        if (i < paths.length) {
            final var current = source.
                    filter(property -> property != null && paths[i].equals(property.getName())).findFirst();
            if (current.isPresent()) {
                return i == paths.length - 1 ?
                        current : getProperty(i + 1, paths, current.get().getProperties());
            }
        }

        return Optional.empty();
    }

    /**
     * Provides methods to make building of configuration and property objects easier.
     */
    final class ConfigBuilder {

        private ConfigBuilder() {
            throw new AssertionError(CREATE_HELPER_CLASS_ERROR);
        }

        /**
         * Returns attributes which belong to configurations.
         *
         * @param jsonObject a raw json object.
         * @return attributes as a map.
         */
        static Optional<Map<String, String>> getAttributes(final JsonObject jsonObject) {
            final var jsonAttributes = (JsonObject) jsonObject.get("attributes");
            if (jsonAttributes != null) {
                final var attributes = new HashMap<String, String>();
                for (final var key : jsonAttributes.keySet()) {
                    attributes.put(key, (String) jsonAttributes.get(key));
                }

                return Optional.of(attributes);
            }

            return Optional.empty();
        }

        /**
         * Returns properties which belong to configurations.
         *
         * @param jsonObject a raw json object.
         * @return properties as a stream.
         */
        static Stream<Property> getProperties(final JsonObject jsonObject) {
            final var jsonProperties = (JsonArray) jsonObject.get("properties");
            return jsonProperties != null ?
                    jsonProperties.stream().map(json -> new Property.Builder((JsonObject) json).build()) :
                    Stream.empty();
        }

        /**
         * Returns a value of the parameter name.
         *
         * @param jsonObject a raw json object.
         * @param name       a parameter name.
         * @return a value.
         */
        static long getLong(final JsonObject jsonObject, final String name) {
            final var value = jsonObject.get(name);
            return value != null ? ((BigDecimal) value).longValue() : 0;
        }

        /**
         * Deletes properties which belong to configurations.
         *
         * @param paths  path to properties.
         * @param stream a stream of source properties.
         * @return updated properties.
         */
        static Collection<Property> deleteProperties(final String[] paths, final Stream<Property> stream) {
            return deleteByPath(0, paths, stream);
        }

        /**
         * Sets properties which belong to configurations.
         *
         * @param paths  path to properties.
         * @param source properties to set.
         */
        static void setProperties(final Collection<Property> target, final String[] paths,
                                  final Collection<Property> source) {
            final var propertyPaths = Validator.of(paths).get();
            if (propertyPaths.length > 0) {
                setByPath(target, 0, paths, source);
            } else {
                target.addAll(Validator.of(source).get());
            }
        }

        private static Collection<Property> deleteByPath(final int i, final String[] paths, final Stream<Property> stream) {
            final var properties = new LinkedList<Property>();
            if (i < paths.length) {
                stream.forEach(property -> {
                    if (property != null) {
                        if (paths[i].equalsIgnoreCase(property.getName())) {
                            final var props = deleteByPath(i + 1, paths, property.getProperties());
                            if (props.size() > 0) {
                                properties.add(new Property.Builder(property).properties(props).build());
                            }
                        } else {
                            properties.add(property);
                        }
                    }
                });
            }

            return properties;
        }

        private static void setByPath(final Collection<Property> target, final int i, final String[] paths,
                                      final Collection<Property> source) {
            if (i < paths.length) {
                final var next = i + 1;
                final var current = target.stream().
                        filter(property -> property != null && paths[i].equals(property.getName())).findFirst();
                if (current.isPresent()) {
                    setByPath(current.get().properties(), next, paths, source);
                } else {
                    final var newProperty = new Property.Builder(paths[i], "").build();
                    target.add(newProperty);
                    setByPath(newProperty.properties(), next, paths, source);
                }
            } else {
                target.addAll(source);
            }
        }
    }
}
