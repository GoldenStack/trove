package dev.goldenstack.loot.criterion;

import com.google.gson.*;
import dev.goldenstack.loot.json.JsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a criterion that stores information about which properties it will accept, and has the ability to return
 * true or false (via {@link #test(Map)}) to test whether or not the provided properties are compatible.<br>
 * By default, it supports two ways of storing data about which properties it will accept: {@link SingleValueProperty}
 * and {@link NumberRangeProperty}.
 */
public record PropertiesCriterion(@NotNull List<Property> properties) implements Predicate<Map<String, String>> {

    /**
     * A static PropertiesCriterion instance that contains no information, so accepts any properties.
     */
    public static final @NotNull PropertiesCriterion ALL = new PropertiesCriterion(List.of());

    public PropertiesCriterion {
        properties = List.copyOf(properties);
    }

    /**
     * Tests this PropertiesCriterion against the provided map of properties.
     */
    @Override
    public boolean test(@NotNull Map<String, String> map) {
        for (Property property : this.properties) {
            String value = map.get(property.key);
            if (!property.applies(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Serializes this PropertiesCriterion into a JsonElement. If this instance has no properties, it returns
     * {@link JsonNull#INSTANCE}.
     */
    public @NotNull JsonElement serialize() throws JsonParseException {
        if (this.properties.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject object = new JsonObject();
        for (Property property : this.properties) {
            object.add(property.key(), property.serialize());
        }
        return object;
    }

    /**
     * Deserializes the provided JsonElement into a PropertiesCriterion. If the element is null according to
     * {@link JsonHelper#isNull(JsonElement)}, it returns {@link PropertiesCriterion#ALL}. Otherwise, it assures the
     * element is a JsonObject and deserializes each key individually with {@link Property#deserialize(String, JsonElement)}.
     */
    public static @NotNull PropertiesCriterion deserialize(@Nullable JsonElement element) throws JsonParseException {
        if (JsonHelper.isNull(element)) {
            return ALL;
        }
        JsonObject object = JsonHelper.assureJsonObject(element, null);

        var entrySet = object.entrySet();
        List<Property> list = new ArrayList<>(entrySet.size());

        for (Map.Entry<String, JsonElement> entry : entrySet) {
            Property pv = Property.deserialize(entry.getKey(), entry.getValue());
            if (pv != null) {
                list.add(Property.deserialize(entry.getKey(), entry.getValue()));
            }
        }
        if (list.size() == 0) {
            return PropertiesCriterion.ALL;
        }
        return new PropertiesCriterion(list);
    }

    /**
     * An abstract class that represents a property.
     */
    public static abstract class Property {
        private final @NotNull String key;

        public Property(@NotNull String key) {
            this.key = key;
        }

        /**
         * Returns this Property's key.
         */
        public @NotNull String key() {
            return key;
        }

        /**
         * Returns whether or not this Property applies to the provided value.
         */
        public abstract boolean applies(@NotNull String value);

        /**
         * Serializes this Property into a JsonElement.
         */
        public abstract @NotNull JsonElement serialize();

        /**
         * Attempts to deserialize the element, along with the provided key, via {@link SingleValueProperty#attemptDeserialization(String, JsonElement)}
         * and {@link NumberRangeProperty#attemptDeserialization(String, JsonElement)}. If neither of them can parse the
         * value, it returns null.
         */
        public static @Nullable Property deserialize(@NotNull String key, @NotNull JsonElement value) {
            if (JsonHelper.isNull(value)) {
                return null;
            }

            Property singleValueProperty = SingleValueProperty.attemptDeserialization(key, value);
            if (singleValueProperty != null) {
                return singleValueProperty;
            }

            return NumberRangeProperty.attemptDeserialization(key, value);
        }
    }

    /**
     * A property with a singular value that is compared directly.<br>
     * The reason this class uses {@code JsonPrimitive}s instead of Strings is so that, when it has to be serialized,
     * it'll stay in the same form. Basically, this method makes sure numbers and booleans don't get converted to
     * strings upon serialization.
     */
    public static class SingleValueProperty extends Property {
        private final @NotNull JsonPrimitive value;

        /**
         * Attempts to deserialize the provided key and element. If it cannot be deserialized, it returns null.
         */
        public static @Nullable SingleValueProperty attemptDeserialization(@NotNull String key, @NotNull JsonElement element) {
            if (element.isJsonPrimitive()) {
                return new SingleValueProperty(key, element.getAsJsonPrimitive());
            }
            return null;
        }

        /**
         * Creates a new SingleValueProperty with the provided key and value.
         */
        public SingleValueProperty(@NotNull String key, @NotNull JsonPrimitive value) {
            super(key);
            this.value = value;
        }

        /**
         * Returns this SingleValueProperty's value.
         */
        public @NotNull JsonPrimitive value() {
            return value;
        }

        /**
         * Returns true if this value is equal to the provided value.
         * <br>Internally, this calls {@code this.value.getAsString().equals(value)}.
         */
        @Override
        public boolean applies(@NotNull String value) {
            return this.value.getAsString().equals(value);
        }

        /**
         * Serializes this SingleValueProperty by just returning the value.
         */
        @Override
        public @NotNull JsonElement serialize() {
            return value;
        }

        @Override
        public String toString() {
            return "SingleValueProperty[key=" + key() + ", value=" + value + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SingleValueProperty that = (SingleValueProperty) o;
            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return key().hashCode() + 31 * value.hashCode();
        }
    }

    /**
     * Represents a Property with a minimum and a maximum value. Either (or neither) of the values can be null, but they
     * cannot both be null.
     */
    public static class NumberRangeProperty extends Property {
        private final @Nullable Number min, max;

        /**
         * Attempts to deserialize the provided key and element. If it cannot be deserialized, it returns null.
         */
        public static @Nullable NumberRangeProperty attemptDeserialization(@NotNull String key, @NotNull JsonElement element) {
            if (!element.isJsonObject()) {
                return null;
            }
            JsonObject object = element.getAsJsonObject();
            var set = object.entrySet();
            if (set.size() == 0 || set.size() > 2) {
                return null;
            }
            Number min = null, max = null;
            JsonPrimitive jel;
            for (var entry : set) {
                if (JsonHelper.isNull(entry.getValue()) || !entry.getValue().isJsonPrimitive()) {
                    return null;
                }
                jel = entry.getValue().getAsJsonPrimitive();
                if (!jel.isNumber()) {
                    return null;
                }
                if (entry.getKey().equals("min")) {
                    min = jel.getAsNumber();
                } else if (entry.getKey().equals("max")) {
                    max = jel.getAsNumber();
                } else {
                    return null;
                }
            }
            if (min == null && max == null) {
                return null;
            }
            return new NumberRangeProperty(key, min, max);
        }

        /**
         * Creates a new NumberRangeProperty with the provided key, minimum, and maximum. Make sure that at least one of
         * minimum or maximum is not null, because if they are both null, an exception will be thrown!
         */
        public NumberRangeProperty(@NotNull String key, @Nullable Number min, @Nullable Number max) {
            super(key);
            if (min == null && max == null) {
                throw new IllegalArgumentException("Minimum and maximum values cannot both be null!");
            }
            this.min = min;
            this.max = max;
        }

        /**
         * Returns the number that represents the minimum value. This may be null, but this and {@link #max()} can not
         * both be null.
         */
        public @Nullable Number min() {
            return min;
        }

        /**
         * Returns the number that represents the maximum value. This may be null, but this and {@link #min()} can not
         * both be null.
         */
        public @Nullable Number max() {
            return max;
        }

        /**
         * Returns true if all of the following are also true:
         * <ul>
         *     <li>The {@code value} is a number</li>
         *     <li>{@link #min()} is null or {@code min} is greater than or equal to the value</li>
         *     <li>{@link #max()} is null or {@code max} is less than or equal to the value</li>
         * </ul>
         */
        @Override
        public boolean applies(@NotNull String value) {
            int i;
            try {
                i = Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                return false;
            }
            if (min != null && i < min.intValue()) {
                return false;
            }
            return max == null || !(i > max.intValue());
        }

        /**
         * Serializes this NumberRangeProperty into a JsonElement. Fields are only serialized if they are not null.
         */
        @Override
        public @NotNull JsonElement serialize() {
            JsonObject object = new JsonObject();
            if (this.min != null) {
                object.addProperty("min", this.min);
            }
            if (this.max != null) {
                object.addProperty("max", this.max);
            }
            return object;
        }

        @Override
        public String toString() {
            return "NumberRangeProperty[key=" + key() + ", min=" + min + ", max=" + max + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NumberRangeProperty that = (NumberRangeProperty) o;
            return Objects.equals(min, that.min) && Objects.equals(max, that.max);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(min) * 31 + Objects.hashCode(max);
        }
    }
}