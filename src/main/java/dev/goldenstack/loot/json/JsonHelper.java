package dev.goldenstack.loot.json;

import com.google.gson.*;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Utility class to help with JSON parsing.
 */
public class JsonHelper {
    private JsonHelper(){
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * @return The error message, for {@code JsonParseException}s, when an element should not have been null.
     */
    public static @NotNull String notNullMessage(@NotNull String key){
        return "Expected value for key \""+key+"\" but found null!";
    }

    /**
     * @return The error message, for {@code JsonParseException}s, when an element was not a JsonPrimitive but should
     * have been.
     */
    public static @NotNull String notJsonPrimitiveMessage(@NotNull String key){
        return "Expected primitive value (string, number, boolean) for key \""+key+"\" but found complex object!";
    }

    /**
     * @return The error message, for {@code JsonParseException}s, when an element was not a JsonObject but should
     * have been.
     */
    public static @NotNull String notJsonObjectMessage(@NotNull String key){
        return "Expected object for key \""+key+"\" but found other value (primitive, array, null, etc)!";
    }

    /**
     * @return The error message, for {@code JsonParseException}s, when an element was not a JsonPrimitive and a string
     * but should have been.
     */
    public static @NotNull String notStringMessage(@NotNull String key){
        return "Expected string for key \""+key+"\" but found other value (number, object, array, boolean, etc)!";
    }

    /**
     * @return The error message, for {@code JsonParseException}s, when an element was not a JsonPrimitive and a boolean
     * but should have been.
     */
    public static @NotNull String notBooleanMessage(@NotNull String key){
        return "Expected boolean for key \""+key+"\" but found other value (number, object, array, string, etc)!";
    }

    public static @NotNull String notNumberMessage(@NotNull String key){
        return "Expected number for key \""+key+"\" but found other value (boolean, object, array, string, etc)!";
    }

    /**
     * @return The error message, for {@code JsonParseException}s, when an element was not a JsonPrimitive and a valid
     * {@code NamespaceID} but should have been.
     */
    public static @NotNull String notNamespaceIdMessage(@NotNull String key){
        return "Expected Namespace ID at key \""+key+"\" but received string with more than one colon!";
    }

    /**
     * @return The error message, for {@code JsonParseException}s, when an element was not a JsonPrimitive and a valid
     * {@code UUID} but should have been.
     */
    public static @NotNull String notUuidMessage(@NotNull String key){
        return "Expected UUID for key \""+key+"\" but found other value (number, object, array, boolean, etc)!";
    }

    /**
     * @return The error message, for {@code JsonParseException}s, when an element was not a JsonPrimitive and a valid
     * {@code NamespaceID} but should have been.
     */
    public static @NotNull String invalidUuidMessage(@NotNull String key, @NotNull String value){
        return "Expected UUID for key \""+key+"\" but found invalid value \""+value+"\"!";
    }

    /**
     * @return The error message, for {@code JsonParseException}s, when an element was not a JsonArray but should have
     * been.
     */
    public static @NotNull String notJsonArrayMessage(@NotNull String key){
        return "Expected array for key \""+key+"\" but found other value (primitive, object, null, etc)!";
    }

    /**
     * Utility method to test if a JsonElement is null or is JsonNull<br>
     * @param element The element to test
     * @return True if the element is null or is an instance of JsonNull, otherwise false
     */
    public static boolean isNull(@Nullable JsonElement element){
        return element == null || element.isJsonNull();
    }

    /**
     * Throws a {@code JsonParseException} if the element is null according to {@link #isNull(JsonElement)}
     * @param element The element to test
     * @param key The key (for creating the error message)
     * @return The element, if it's not null
     * @throws JsonParseException if the element is null according to {@link #isNull(JsonElement)}
     */
    public static @NotNull JsonElement assureNotNull(@Nullable JsonElement element, @NotNull String key) throws JsonParseException {
        if (isNull(element)){
            throw new JsonParseException(notNullMessage(key));
        }
        return element;
    }

    /**
     * Throws a JsonParseException if the element is not a JsonPrimitive
     * @param element A not-null JsonElement
     * @param key The key (for creating the error message)
     * @return The element as a JsonPrimitive
     * @throws JsonParseException if the element was not a JsonPrimitive
     */
    public static @NotNull JsonPrimitive assureJsonPrimitive(@NotNull JsonElement element, @NotNull String key) throws JsonParseException {
        if (!element.isJsonPrimitive()){
            throw new JsonParseException(notJsonPrimitiveMessage(key));
        }
        return element.getAsJsonPrimitive();
    }

    /**
     * Throws a JsonParseException if the element is not a JsonObject
     * @param element A not-null JsonElement
     * @param key The key (for creating the error message)
     * @return The element as a JsonObject
     * @throws JsonParseException if the element was not a JsonObject
     */
    public static @NotNull JsonObject assureJsonObject(@NotNull JsonElement element, @NotNull String key) throws JsonParseException {
        if (!element.isJsonObject()){
            throw new JsonParseException(notJsonObjectMessage(key));
        }
        return element.getAsJsonObject();
    }

    /**
     * Throws a JsonParseException if the element is not a JsonPrimitive that is a String
     * @param element A not-null JsonElement
     * @param key The key (for creating the error message)
     * @return The element as a String
     * @throws JsonParseException if the element was not a JsonPrimitive that is a String
     */
    public static @NotNull String getString(@NotNull JsonElement element, @NotNull String key) throws JsonParseException {
        final JsonPrimitive primitive = assureJsonPrimitive(element, key);
        if (primitive.isString()){
            return primitive.getAsString();
        } else {
            throw new JsonParseException(notStringMessage(key));
        }
    }

    /**
     * Throws a JsonParseException if the element is not a JsonPrimitive that is a boolean
     * @param element A not-null JsonElement
     * @param key The key (for creating the error message)
     * @return The element as a boolean
     * @throws JsonParseException if the element was not a JsonPrimitive that is a boolean
     */
    public static boolean getBoolean(@NotNull JsonElement element, @NotNull String key) throws JsonParseException {
        final JsonPrimitive primitive = assureJsonPrimitive(element, key);
        if (primitive.isBoolean()){
            return primitive.getAsBoolean();
        } else {
            throw new JsonParseException(notBooleanMessage(key));
        }
    }

    /**
     * Throws a JsonParseException if the element is not a JsonPrimitive that is a number
     * @param element A not-null JsonElement
     * @param key The key (for creating the error message)
     * @return The element as a Number
     * @throws JsonParseException if the element was not a JsonPrimitive that is a number
     */
    public static @NotNull Number getNumber(@NotNull JsonElement element, @NotNull String key) throws JsonParseException {
        final JsonPrimitive primitive = assureJsonPrimitive(element, key);
        if (primitive.isNumber()){
            return primitive.getAsNumber();
        } else {
            throw new JsonParseException(notNumberMessage(key));
        }
    }



    /**
     * Throws a JsonParseException if the element is not a JsonPrimitive that is a boolean
     * @param element A not-null JsonElement
     * @param key The key (for creating the error message)
     * @return The element as a boolean
     * @throws JsonParseException if the element was not a JsonPrimitive that is a boolean
     */
    public static @NotNull NamespaceID getNamespaceID(@NotNull JsonElement element, @NotNull String key) throws JsonParseException {
        final String id = getString(element, key);
        final int index = id.indexOf(':');
        if (index < 0){
            return NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, id);
        } else if (id.indexOf(':', index + 1) != -1){
            throw new JsonParseException(notNamespaceIdMessage(key));
        } else {
            return NamespaceID.from(id.substring(0, index), id.substring(index + 1));
        }
    }

    /**
     * Throws a JsonParseException if the element is not a String that can be parsed as a UUID
     * @param element A not-null JsonElement
     * @param key The key (for creating the error message)
     * @return The parsed UUID
     * @throws JsonParseException if the element was not a String that can be parsed as a UUID
     */
    public static @NotNull UUID getUUID(@NotNull JsonElement element, @NotNull String key){
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()){
            final String val = element.getAsJsonPrimitive().getAsString();
            try {
                return UUID.fromString(val);
            } catch(IllegalArgumentException exception){
                throw new JsonParseException(invalidUuidMessage(key, val));
            }
        }
        throw new JsonParseException(notUuidMessage(key));
    }

    /**
     * Throws a JsonParseException if the element is not a JsonArray
     * @param element A not-null JsonElement
     * @param key The key (for creating the error message)
     * @return The element as a JsonArray
     * @throws JsonParseException if the element was not a JsonArray
     */
    private static @NotNull JsonArray assureJsonArray(@NotNull JsonElement element, @NotNull String key) throws JsonParseException {
        if (!element.isJsonArray()){
            throw new JsonParseException(notJsonArrayMessage(key));
        }
        return element.getAsJsonArray();
    }
}
