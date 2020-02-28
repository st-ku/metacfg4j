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
package com.github.akarazhev.metaconfig.api;

import com.github.akarazhev.metaconfig.extension.ExtJsonable;
import com.github.akarazhev.metaconfig.extension.Validator;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
     * @param index  a current path.
     * @param paths  paths
     * @param source a current property stream.
     * @return a property.
     */
    static Optional<Property> getProperty(final int index, final String[] paths, final Stream<Property> source) {
        if (index < paths.length) {
            final Optional<Property> current = source.
                    filter(property -> paths[index].equals(property.getName())).findFirst();
            if (current.isPresent()) {
                return index == paths.length - 1 ?
                        current : getProperty(index + 1, paths, current.get().getProperties());
            }
        }

        return Optional.empty();
    }

    /**
     * Provides methods to make building of configuration and property objects easier.
     */
    final class ConfigBuilder {
        /**
         * Returns attributes which belong to configurations.
         *
         * @param jsonObject a raw json object.
         * @return attributes as a map.
         */
        static Optional<Map<String, String>> getAttributes(final JsonObject jsonObject) {
            final JsonObject jsonAttributes = (JsonObject) jsonObject.get("attributes");
            if (jsonAttributes != null) {
                final Map<String, String> attributes = new HashMap<>();
                for (final Object key : jsonAttributes.keySet()) {
                    attributes.put((String) key, (String) jsonAttributes.get(key));
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
            final JsonArray jsonProperties = (JsonArray) jsonObject.get("properties");
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
            final Object value = jsonObject.get(name);
            return value != null ? ((BigDecimal) value).longValue() : 0;
        }

        /**
         * Sets properties which belong to configurations.
         *
         * @param paths  path to properties.
         * @param source properties to set.
         */
        static void setProperties(final Collection<Property> target, final String[] paths,
                                  final Collection<Property> source) {
            final String[] propertyPaths = Validator.of(paths).get();
            if (propertyPaths.length > 0) {
                setProperties(target, 0, paths, source);
            } else {
                target.addAll(Validator.of(source).get());
            }
        }

        private static void setProperties(final Collection<Property> target, final int index, final String[] paths,
                                          final Collection<Property> source) {
            if (index < paths.length) {
                final int nextIndex = index + 1;
                final Optional<Property> current = target.stream().
                        filter(property -> paths[index].equals(property.getName())).findFirst();
                if (current.isPresent()) {
                    setProperties(current.get().getProps(), nextIndex, paths, source);
                } else {
                    final Property newProperty = new Property.Builder(paths[index], "").build();
                    target.add(newProperty);
                    setProperties(newProperty.getProps(), nextIndex, paths, source);
                }
            } else {
                target.addAll(source);
            }
        }
    }
}
