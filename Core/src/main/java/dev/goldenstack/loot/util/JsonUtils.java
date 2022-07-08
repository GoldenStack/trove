package dev.goldenstack.loot.util;

import com.google.gson.*;
import dev.goldenstack.loot.conversion.LootConversionException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to help with JSON serialization and deserialization.
 */
public class JsonUtils {

    private JsonUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    /**
     * @param element the element that will be tested
     * @return true if the element is null or is an instance of {@link JsonNull}, otherwise false
     */
    @Contract("null -> true")
    public static boolean isNull(@Nullable JsonElement element) {
        return element == null || element.isJsonNull();
    }

    /**
     * @param element the element that will possibly be converted
     * @return the element as a JsonPrimitive, or null if it's not a JsonPrimitive
     */
    public static @Nullable JsonPrimitive getAsJsonPrimitive(@Nullable JsonElement element) {
        return (element != null && element.isJsonPrimitive()) ? element.getAsJsonPrimitive() : null;
    }

    /**
     * @param element the element that will possibly be converted
     * @return The element as a JsonObject, or null if it's not a JsonObject
     */
    public static @Nullable JsonObject getAsJsonObject(@Nullable JsonElement element) {
        return (element != null && element.isJsonObject()) ? element.getAsJsonObject() : null;
    }

    /**
     * @param element the element that will possibly be converted
     * @return The element as a JsonArray, or null if it's not a JsonArray
     */
    public static @Nullable JsonArray getAsJsonArray(@Nullable JsonElement element) {
        return (element != null && element.isJsonArray()) ? element.getAsJsonArray() : null;
    }

    /**
     * @param element the element that will possibly be converted
     * @return the element as a Boolean, or null if it's not a Boolean
     */
    public static @Nullable Boolean getAsBoolean(@Nullable JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        return null;
    }

    /**
     * @param element the element that will possibly be converted
     * @return the element as a Number, or null if it's not a Number
     */
    public static @Nullable Number getAsNumber(@Nullable JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return primitive.getAsNumber();
        }
        return null;
    }

    /**
     * @param element the element that will possibly be converted
     * @return the element as a String, or null if it's not a String
     */
    public static @Nullable String getAsString(@Nullable JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isString()) {
            return primitive.getAsString();
        }
        return null;
    }

    /**
     * Gets the element type of the provided JsonElement as a string. This is purely for debug messages.<br>
     * For example, a JsonArray would return "array", and a JsonPrimitive that is a number would return "number". See
     * {@link #singularElementType(JsonElement)} because it might be more useful.
     * @param element the element to find the type of
     * @return the type of the element
     */
    public static @NotNull String getElementType(@Nullable JsonElement element) {
        if (isNull(element)) {
            return "null";
        } else if (element.isJsonObject()) {
            return "object";
        } else if (element.isJsonArray()) {
            return "array";
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return "number";
            } else if (primitive.isString()) {
                return "string";
            } else if (primitive.isBoolean()) {
                return "boolean";
            }
        }
        return "unknown";
    }

    /**
     * Gets the element type of the provided JsonElement as a string. This is purely for debug messages.<br>
     * This is different from {@link #getElementType(JsonElement)} because it provides a singular form of the message.
     * For example, a JsonArray would return "an array", and a JsonPrimitive that is a number would return "a number".
     * @param element the element to find the type of
     * @return the type of the element
     */
    public static @NotNull String singularElementType(@Nullable JsonElement element) {
        if (isNull(element)){
            return "a null value";
        } else if (element.isJsonObject()) {
            return "an object";
        } else if (element.isJsonArray()){
            return "an array";
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return "a number";
            } else if (primitive.isString()) {
                return "a string";
            } else if (primitive.isBoolean()) {
                return "a boolean";
            }
        }
        return "an unknown value";
    }

    /**
     * Creates an error message based on the provided arguments. This method adjusts the message if {@code key} or
     * {@code element} is null, but make sure that {@code expected} is never null. This is the code backing all error
     * message creation in this library.<br>
     * Here are some example return values:
     * <ul>
     *     <li>("abc", null, null) -> "Expected abc!"</li>
     *     <li>("abc", null, new JsonPrimitive("ghi")) -> "Expected abc but found a string!"</li>
     *     <li>("abc", null, new JsonPrimitive(true)) -> "Expected abc but found a boolean!"</li>
     *     <li>("abc", null, new JsonPrimitive(123)) -> "Expected abc but found a number!"</li>
     *     <li>("abc", null, new JsonObject()) -> "Expected abc but found an object!"</li>
     *     <li>("abc", "def", null) -> "Expected abc for key "def"!"</li>
     *     <li>("abc", "def", new JsonPrimitive("ghi") -> "Expected abc for key "def" but found a string!"</li>
     * </ul>
     * @param expected what was being expected
     * @param key the key (can be null)
     * @param element the element (can be null)
     * @return the error message that was created.
     */
    public static @NotNull String createExpectedValueMessage(@NotNull String expected, @Nullable String key, @Nullable JsonElement element) {
        StringBuilder builder = new StringBuilder("Expected ").append(expected);
        if (key != null) {
            builder.append(" for key \"").append(key).append("\"");
        }
        if (element != null) {
            builder.append(" but found ").append(singularElementType(element));
        }
        return builder.append('!').toString();
    }

    /**
     * This is useful for distinguishing between an unknown element and a null element for {@link #createExpectedValueMessage(String, String, JsonElement)}.
     * @param element the element to possibly convert to {@link JsonNull#INSTANCE}
     * @return {@link JsonNull#INSTANCE} if {@code element} is null. Otherwise, returns {@code element}
     */
    public static @NotNull JsonElement jsonNullifNull(@Nullable JsonElement element) {
        return element == null ? JsonNull.INSTANCE : element;
    }

    /**
     * Creates an error message as though the provided element was not supposed to be null.
     * @param key the key of the element, which will be used to potentially provide more accurate error messages
     * @param element the element that will be used for the error message
     * @return {@code createExpectedValueMessage("a not-null value", key, element);}
     */
    public static @NotNull String expectedNotNullMessage(@Nullable String key, @Nullable JsonElement element) {
        return createExpectedValueMessage("a not-null value", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be null.
     * @param key the key of the element, which will be used to potentially provide more accurate error messages
     * @param element the element that will be used for the error message
     * @return {@code createExpectedValueMessage("a null value", key, element);}
     */
    public static @NotNull String expectedNullMessage(@Nullable String key, @Nullable JsonElement element) {
        return createExpectedValueMessage("a null value", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a JsonPrimitive.
     * @param key the key of the element, which will be used to potentially provide more accurate error messages
     * @param element the element that will be used for the error message
     * @return {@code createExpectedValueMessage("a primitive (string, number, or boolean)", key, element);}
     */
    public static @NotNull String expectedJsonPrimitiveMessage(@Nullable String key, @Nullable JsonElement element) {
        return createExpectedValueMessage("a primitive (string, number, or boolean)", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a JsonObject.
     * @param key the key of the element, which will be used to potentially provide more accurate error messages
     * @param element the element that will be used for the error message
     * @return {@code createExpectedValueMessage("an object", key, element);}
     */
    public static @NotNull String expectedJsonObjectMessage(@Nullable String key, @Nullable JsonElement element) {
        return createExpectedValueMessage("an object", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a JsonArray.
     * @param key the key of the element, which will be used to potentially provide more accurate error messages
     * @param element the element that will be used for the error message
     * @return {@code createExpectedValueMessage("an array", key, element);}
     */
    public static @NotNull String expectedJsonArrayMessage(@Nullable String key, @Nullable JsonElement element) {
        return createExpectedValueMessage("an array", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a boolean.
     * @param key the key of the element, which will be used to potentially provide more accurate error messages
     * @param element the element that will be used for the error message
     * @return {@code createExpectedValueMessage("a boolean", key, element);}
     */
    public static @NotNull String expectedBooleanMessage(@Nullable String key, @Nullable JsonElement element) {
        return createExpectedValueMessage("a boolean", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a number.
     * @param key the key of the element, which will be used to potentially provide more accurate error messages
     * @param element the element that will be used for the error message
     * @return {@code createExpectedValueMessage("a number", key, element);}
     */
    public static @NotNull String expectedNumberMessage(@Nullable String key, @Nullable JsonElement element) {
        return createExpectedValueMessage("a number", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a string.
     * @param key the key of the element, which will be used to potentially provide more accurate error messages
     * @param element the element that will be used for the error message
     * @return {@code createExpectedValueMessage("a string", key, element);}
     */
    public static @NotNull String expectedStringMessage(@Nullable String key, @Nullable JsonElement element) {
        return createExpectedValueMessage("a string", key, element);
    }

    /**
     * Throws a {@code LootConversionException} if the element is null according to {@link #isNull(JsonElement)}
     * @param element the element to test
     * @param key the key (optional, for creating the error message)
     * @return the element, if it's not null
     * @throws LootConversionException if the element is null according to {@link #isNull(JsonElement)}
     */
    @Contract("null, _ -> fail")
    public static @NotNull JsonElement assureNotNull(@Nullable JsonElement element, @NotNull String key) throws LootConversionException {
        if (isNull(element)){
            throw new LootConversionException(expectedNotNullMessage(key, element));
        }
        return element;
    }

    /**
     * Throws a {@code LootConversionException} if the element is not a JsonPrimitive
     * @param element the element to test
     * @param key the key (optional, for creating the error message)
     * @return the element as a JsonPrimitive
     * @throws LootConversionException if the element was not a JsonPrimitive
     */
    @Contract("null, _ -> fail")
    public static @NotNull JsonPrimitive assureJsonPrimitive(@Nullable JsonElement element, @Nullable String key) throws LootConversionException {
        JsonPrimitive primitive = getAsJsonPrimitive(element);
        if (primitive == null) {
            throw new LootConversionException(expectedJsonPrimitiveMessage(key, jsonNullifNull(element)));
        }
        return primitive;
    }

    /**
     * Throws a {@code LootConversionException} if the element is not a JsonObject
     * @param element the element to test
     * @param key the key (optional, for creating the error message)
     * @return the element as a JsonObject
     * @throws LootConversionException if the element was not a JsonObject
     */
    @Contract("null, _ -> fail")
    public static @NotNull JsonObject assureJsonObject(@Nullable JsonElement element, @Nullable String key) throws LootConversionException {
        JsonObject object = getAsJsonObject(element);
        if (object == null) {
            throw new LootConversionException(expectedJsonObjectMessage(key, jsonNullifNull(element)));
        }
        return object;
    }

    /**
     * Throws a {@code LootConversionException} if the element is not a JsonArray
     * @param element the element to test
     * @param key the key (optional, for creating the error message)
     * @return the element as a JsonArray
     * @throws LootConversionException if the element was not a JsonArray
     */
    @Contract("null, _ -> fail")
    public static @NotNull JsonArray assureJsonArray(@Nullable JsonElement element, @Nullable String key) throws LootConversionException {
        JsonArray array = getAsJsonArray(element);
        if (array == null) {
            throw new LootConversionException(expectedJsonArrayMessage(key, jsonNullifNull(element)));
        }
        return array;
    }

    /**
     * Throws a {@code LootConversionException} if the element is not a JsonPrimitive that is a boolean
     * @param element the element to test
     * @param key the key (optional, for creating the error message)
     * @return the element as a boolean
     * @throws LootConversionException if the element was not a boolean
     */
    @Contract("null, _ -> fail")
    public static boolean assureBoolean(@Nullable JsonElement element, @Nullable String key) throws LootConversionException {
        Boolean bool = getAsBoolean(element);
        if (bool == null) {
            throw new LootConversionException(expectedBooleanMessage(key, jsonNullifNull(element)));
        }
        return bool;
    }

    /**
     * Throws a {@code LootConversionException} if the element is not a JsonPrimitive that is a number
     * @param element the element to test
     * @param key the key (optional, for creating the error message)
     * @return the element as a number
     * @throws LootConversionException if the element was not a number
     */
    @Contract("null, _ -> fail")
    public static @NotNull Number assureNumber(@Nullable JsonElement element, @Nullable String key) throws LootConversionException {
        Number number = getAsNumber(element);
        if (number == null) {
            throw new LootConversionException(expectedNumberMessage(key, jsonNullifNull(element)));
        }
        return number;
    }

    /**
     * Throws a {@code LootConversionException} if the element is not a JsonPrimitive that is a string
     * @param element the element to test
     * @param key the key (optional, for creating the error message)
     * @return the element as a string
     * @throws LootConversionException if the element was not a string
     */
    @Contract("null, _ -> fail")
    public static @NotNull String assureString(@Nullable JsonElement element, @Nullable String key) throws LootConversionException {
        String string = getAsString(element);
        if (string == null) {
            throw new LootConversionException(expectedStringMessage(key, jsonNullifNull(element)));
        }
        return string;
    }

    /**
     * Serializes the provided list into a JSON array
     * @param elements the elements to serialize into the array
     * @param serializer the serializer that will be individually applied to each element in {@code elements}.
     * @return the array of serialized elements
     * @param <T> the class of the items that are being serialized
     * @throws LootConversionException if the serializer throws any exceptions
     */
    public static @NotNull <T> JsonArray serializeJsonArray(@NotNull List<T> elements, @NotNull UtilitySerializer<T> serializer) throws LootConversionException {
        final JsonArray array = new JsonArray();
        for (T item : elements) {
            array.add(serializer.serialize(item));
        }
        return array;
    }

    /**
     * Deserializes the provided JSON array into a list of elements via the provided deserializer. The returned list may
     * or may not be immutable.
     * @param array the array that will hopefully be deserialized
     * @param key the key of the array, which will be used for potentially more accurate error messages
     * @param deserializer the deserializer that will be individually applied to each element in {@code array}.
     * @return the list of deserialized elements
     * @param <T> the class of the items that are being deserialized
     * @throws LootConversionException if the deserializer throws any exceptions
     */
    public static @NotNull <T> List<T> deserializeJsonArray(@NotNull JsonArray array, @Nullable String key, @NotNull UtilityDeserializer<T> deserializer) throws LootConversionException {
        if (array.size() == 0) {
            return List.of();
        }
        List<T> list = new ArrayList<>();
        for (JsonElement element : array) {
            list.add(deserializer.deserialize(element, key == null ? "" : key + " (while deserializing array elements)"));
        }
        return list;
    }

    // Unfortunately, this must be done because of checked exceptions

    /**
     * Note: this interface only exists because {@link LootConversionException} is a checked exception and, thus,
     * {@link java.util.function.Function} can not be used.
     * @param <T> the class that JSON elements will potentially be serialized to
     */
    @FunctionalInterface
    public interface UtilitySerializer<T> {

        /**
         * @param t the instance of {@link T} that must be serialized
         * @return the serialized element
         * @throws LootConversionException if the input could not be serialized
         */
        @NotNull JsonElement serialize(@NotNull T t) throws LootConversionException;

    }

    /**
     * Note: this interface only exists because {@link LootConversionException} is a checked exception and, thus,
     * {@link java.util.function.BiFunction} can not be used.
     * @param <T> the class that JSON elements will potentially be deserialized to
     */
    @FunctionalInterface
    public interface UtilityDeserializer<T> {

        /**
         * @param element the element to deserialize into another object
         * @param key the key of the element, which will be used for more informative error messages
         * @return the deserialized object
         * @throws LootConversionException if the element could not be deserialized
         */
        @NotNull T deserialize(@NotNull JsonElement element, @Nullable String key) throws LootConversionException;

    }

}
