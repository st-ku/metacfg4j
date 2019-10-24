package com.github.akarazhev.metaconfig.api;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class Property implements Configurable {

    public static enum Type {
        BOOLEAN,
        DOUBLE,
        LONG,
        STRING,
        STRING_ARRAY
    }

    private final String name;
    private final String caption;
    private final String description;
    private final Type type;
    private final String value;
    private final Map<String, String> attributes;
    private final Collection<Property> properties;

    private Property(final Builder builder) {
        this.name = builder.name;
        this.caption = builder.caption;
        this.description = builder.description;
        this.type = builder.type;
        this.value = builder.value;
        this.attributes = builder.attributes;
        this.properties = builder.properties;
    }

    public String getName() {
        return name;
    }

    public String getCaption() {
        return caption;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public Stream<Property> getProperties() {
        return properties.stream();
    }

    @Override
    public void toJson(Writer writer) throws IOException {
        final JsonObject json = new JsonObject();
        json.put("name", name);
        json.put("caption", caption);
        json.put("description", description);
        json.put("type", type.name());
        json.put("value", value);
        json.put("attributes", attributes);
        json.put("properties", properties);
        json.toJson(writer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Property property = (Property) o;
        return name.equals(property.name) &&
                Objects.equals(caption, property.caption) &&
                Objects.equals(description, property.description) &&
                type == property.type &&
                value.equals(property.value) &&
                Objects.equals(attributes, property.attributes) &&
                Objects.equals(properties, property.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, caption, description, type, value, attributes, properties);
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name + '\'' +
                ", caption='" + caption + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                '}';
    }

    public final static class Builder extends ConfigBuilder {
        private final String name;
        private String caption;
        private String description;
        private final Type type;
        private final String value;
        private Map<String, String> attributes;
        private Collection<Property> properties;

        public Builder(final JsonObject jsonObject) {
            this.name = Objects.requireNonNull((String) jsonObject.get("name"));
            this.caption = (String) jsonObject.get("caption");
            this.description = (String) jsonObject.get("description");
            this.type = Type.valueOf((String) jsonObject.get("type"));
            this.value = (String) jsonObject.get("value");
            getAttributes(jsonObject.get("attributes")).ifPresent(attributes -> this.attributes = attributes);
            getProperties(jsonObject.get("properties")).ifPresent(properties -> this.properties = properties);
        }

        public Builder(final String name, final Type type, final String value) {
            this.name = Objects.requireNonNull(name);
            this.type = Objects.requireNonNull(type);
            this.value = Objects.requireNonNull(value);
        }

        public Builder caption(final String caption) {
            this.caption = Objects.requireNonNull(caption);
            return this;
        }

        public Builder description(final String description) {
            this.description = Objects.requireNonNull(description);
            return this;
        }

        public Builder attributes(final Map<String, String> attributes) {
            this.attributes = new HashMap<>(Objects.requireNonNull(attributes));
            return this;
        }

        public Builder properties(final Collection<Property> properties) {
            this.properties = new ArrayList<>(Objects.requireNonNull(properties));
            return this;
        }

        public Property build() {
            return new Property(this);
        }
    }
}
