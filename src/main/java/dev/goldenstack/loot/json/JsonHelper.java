package dev.goldenstack.loot.json;

import com.google.gson.*;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Utility class to help with JSON serialization and deserialization.
 */
public class JsonHelper {
    private JsonHelper(){
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * Returns {@code true} if the element is null or is an instance of {@link JsonNull}.
     * @param element The element to test
     * @return True if the element is null or is an instance of JsonNull, otherwise false
     */
    public static boolean isNull(@Nullable JsonElement element){
        return element == null || element.isJsonNull();
    }

    /**
     * Gets the element type of the provided JsonElement as a string. This is purely for debug messages.<br>
     * For example, a JsonArray would return "array", and a JsonPrimitive that is a number would return "number". See
     * {@link #singularElementType(JsonElement)} because it might be more useful.
     * @param element The element to find the type of
     * @return The type of the element
     */
    public static @NotNull String getElementType(@Nullable JsonElement element){
        if (isNull(element)){
            return "null";
        } else if (element.isJsonObject()) {
            return "object";
        } else if (element.isJsonArray()){
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
     * @param element The element to find the type of
     * @return The type of the element
     */
    public static @NotNull String singularElementType(@Nullable JsonElement element){
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
     * @param expected What was being expected
     * @param key The key (can be null)
     * @param element The element (can be null)
     * @return The error message that was created.
     */
    public static @NotNull String createExpectedValueMessage(@NotNull String expected, @Nullable String key, @Nullable JsonElement element){
        StringBuilder builder = new StringBuilder("Expected ").append(expected);
        if (key != null){
            builder.append(" for key \"").append(key).append("\"");
        }
        if (element != null){
            builder.append(" but found ").append(singularElementType(element));
        }
        return builder.append('!').toString();
    }

    /**
     * Returns {@link JsonNull#INSTANCE} if {@code element} is null. Otherwise, returns {@code element}.<br>
     * This is useful for distinguishing between an unknown element and a null element for {@link #createExpectedValueMessage(String, String, JsonElement)}.
     */
    public static @NotNull JsonElement jsonNullifNull(@Nullable JsonElement element){
        return element == null ? JsonNull.INSTANCE : element;
    }

    /**
     * Creates an error message as though the provided element was not supposed to be null.<br>
     * Returns {@code createExpectedValueMessage("a not-null value", key, element);}
     */
    public static @NotNull String expectedNotNullMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("a not-null value", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be null.<br>
     * Returns {@code createExpectedValueMessage("a null value", key, element);}
     */
    public static @NotNull String expectedNullMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("a null value", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a JsonPrimitive.<br>
     * Returns {@code createExpectedValueMessage("a primitive (string, number, or boolean)", key, element);}
     */
    public static @NotNull String expectedJsonPrimitiveMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("a primitive (string, number, or boolean)", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a JsonObject.<br>
     * Returns {@code createExpectedValueMessage("an object", key, element);}
     */
    public static @NotNull String expectedJsonObjectMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("an object", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a JsonArray.<br>
     * Returns {@code createExpectedValueMessage("an array", key, element);}
     */
    public static @NotNull String expectedJsonArrayMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("an array", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a boolean.<br>
     * Returns {@code createExpectedValueMessage("a boolean", key, element);}
     */
    public static @NotNull String expectedBooleanMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("a boolean", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a number.<br>
     * Returns {@code createExpectedValueMessage("a number", key, element);}
     */
    public static @NotNull String expectedNumberMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("a number", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a string.<br>
     * Returns {@code createExpectedValueMessage("a string", key, element);}
     */
    public static @NotNull String expectedStringMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("a string", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a valid NamespaceID.<br>
     * Returns {@code createExpectedValueMessage("a NamespaceID", key, element);}
     */
    public static @NotNull String expectedNamespaceIdMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("a NamespaceID", key, element);
    }

    /**
     * Creates an error message as though the provided element was supposed to be a valid UUID.<br>
     * Returns {@code createExpectedValueMessage("a UUID", key, element);}
     */
    public static @NotNull String expectedUuidMessage(@Nullable String key, @Nullable JsonElement element){
        return createExpectedValueMessage("a UUID", key, element);
    }

    /**
     * Throws a {@code JsonParseException} if the element is null according to {@link #isNull(JsonElement)}
     * @param element The element to test
     * @param key The key (optional, for creating the error message)
     * @return The element, if it's not null
     * @throws JsonParseException if the element is null according to {@link #isNull(JsonElement)}
     */
    @Contract("null, _ -> fail")
    public static @NotNull JsonElement assureNotNull(@Nullable JsonElement element, @NotNull String key) throws JsonParseException {
        if (isNull(element)){
            throw new JsonParseException(expectedNotNullMessage(key, element));
        }
        return element;
    }

    /**
     * Throws a {@code JsonParseException} if the element is not a JsonPrimitive
     * @param element The element to test
     * @param key The key (optional, for creating the error message)
     * @return The element as a JsonPrimitive
     * @throws JsonParseException if the element was not a JsonPrimitive
     */
    @Contract("null, _ -> fail")
    public static @NotNull JsonPrimitive assureJsonPrimitive(@Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        if (element == null || !element.isJsonPrimitive()){
            throw new JsonParseException(expectedJsonPrimitiveMessage(key, jsonNullifNull(element)));
        }
        return element.getAsJsonPrimitive();
    }

    /**
     * Throws a {@code JsonParseException} if the element is not a JsonObject
     * @param element The element to test
     * @param key The key (optional, for creating the error message)
     * @return The element as a JsonObject
     * @throws JsonParseException if the element was not a JsonObject
     */
    @Contract("null, _ -> fail")
    public static @NotNull JsonObject assureJsonObject(@Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        if (element == null || !element.isJsonObject()){
            throw new JsonParseException(expectedJsonObjectMessage(key, jsonNullifNull(element)));
        }
        return element.getAsJsonObject();
    }

    /**
     * Throws a {@code JsonParseException} if the element is not a JsonArray
     * @param element The element to test
     * @param key The key (optional, for creating the error message)
     * @return The element as a JsonArray
     * @throws JsonParseException if the element was not a JsonArray
     */
    @Contract("null, _ -> fail")
    public static @NotNull JsonArray assureJsonArray(@Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        if (element == null || !element.isJsonArray()){
            throw new JsonParseException(expectedJsonArrayMessage(key, jsonNullifNull(element)));
        }
        return element.getAsJsonArray();
    }

    /**
     * Throws a {@code JsonParseException} if the element is not a JsonPrimitive that is a boolean
     * @param element The element to test
     * @param key The key (optional, for creating the error message)
     * @return The element as a boolean
     * @throws JsonParseException if the element was not a boolean
     */
    @Contract("null, _ -> fail")
    public static boolean assureBoolean(@Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        if (element == null || !element.isJsonPrimitive()){
            throw new JsonParseException(expectedBooleanMessage(key, jsonNullifNull(element)));
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isBoolean()){
            throw new JsonParseException(expectedBooleanMessage(key, element));
        }
        return primitive.getAsBoolean();
    }

    /**
     * Throws a {@code JsonParseException} if the element is not a JsonPrimitive that is a number
     * @param element The element to test
     * @param key The key (optional, for creating the error message)
     * @return The element as a number
     * @throws JsonParseException if the element was not a number
     */
    @Contract("null, _ -> fail")
    public static @NotNull Number assureNumber(@Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        if (element == null || !element.isJsonPrimitive()){
            throw new JsonParseException(expectedNumberMessage(key, jsonNullifNull(element)));
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isNumber()){
            throw new JsonParseException(expectedNumberMessage(key, element));
        }
        return primitive.getAsNumber();
    }

    /**
     * Throws a {@code JsonParseException} if the element is not a JsonPrimitive that is a string
     * @param element The element to test
     * @param key The key (optional, for creating the error message)
     * @return The element as a string
     * @throws JsonParseException if the element was not a string
     */
    @Contract("null, _ -> fail")
    public static @NotNull String assureString(@Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        if (element == null || !element.isJsonPrimitive()){
            throw new JsonParseException(expectedStringMessage(key, jsonNullifNull(element)));
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isString()){
            throw new JsonParseException(expectedStringMessage(key, element));
        }
        return primitive.getAsString();
    }

    /**
     * Throws a {@code JsonParseException} if the element is not a string that is a valid NamespaceID
     * @param element The element to test
     * @param key The key (optional, for creating the error message)
     * @return The element as a NamespaceID
     * @throws JsonParseException if the element was not a valid NamespaceID
     */
    @Contract("null, _ -> fail")
    public static @NotNull NamespaceID assureNamespaceId(@Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        if (element == null || !element.isJsonPrimitive()){
            throw new JsonParseException(expectedNamespaceIdMessage(key, jsonNullifNull(element)));
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isString()){
            throw new JsonParseException(expectedNamespaceIdMessage(key, element));
        }
        String string = primitive.getAsString();
        final int index = string.indexOf(':');
        if (index < 0){
            return NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, string);
        } else if (string.indexOf(':', index + 1) != -1){
            throw new JsonParseException(expectedNamespaceIdMessage(key, element));
        } else {
            return NamespaceID.from(string.substring(0, index), string.substring(index + 1));
        }
    }

    /**
     * Throws a {@code JsonParseException} if the element is not a string that is a valid UUID
     * @param element The element to test
     * @param key The key (optional, for creating the error message)
     * @return The element as a UUID
     * @throws JsonParseException if the element was not a valid UUID
     */
    @Contract("null, _ -> fail")
    public static @NotNull UUID assureUUID(@Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        if (element == null || !element.isJsonPrimitive()){
            throw new JsonParseException(expectedUuidMessage(key, jsonNullifNull(element)));
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isString()){
            throw new JsonParseException(expectedUuidMessage(key, element));
        }
        String string = primitive.getAsString();
        try {
            return UUID.fromString(string);
        } catch (IllegalArgumentException exception){
            throw new JsonParseException(expectedUuidMessage(key, element));
        }
    }

    /**
     * Deserializes an object with the primary key if it exists. If not, it attempts to deserialize with the secondary
     * key. If the secondary key doesn't exist or is null, it deserializes with the first key.
     * @param object The object to get keys from
     * @param deserializer The deserializer
     * @param primary The primary key
     * @param secondary The secondary key
     * @return The deserialized object
     */
    public static @NotNull <T> T optionalAlternativeKey(@NotNull JsonObject object, @NotNull BiFunction<JsonElement, String, T> deserializer,
                                                      @NotNull String primary, @NotNull String secondary){
        JsonElement p = object.get(primary);
        if (!isNull(p)){
            return deserializer.apply(p, primary);
        }
        JsonElement s = object.get(secondary);
        if (!isNull(s)){
            return deserializer.apply(s, secondary);
        }
        return deserializer.apply(p, primary);
    }
}
