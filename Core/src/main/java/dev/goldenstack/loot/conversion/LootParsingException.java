package dev.goldenstack.loot.conversion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A generic exception for an issue that has occurred during loot parsing.
 */
public class LootParsingException extends Exception {

    /**
     * Creates a new instance from the provided arguments.
     * @param message this exception's error message
     */
    public LootParsingException(@NotNull String message) {
        super(message);
    }

    /**
     * Creates a new instance from the provided arguments.
     * @param message this exception's error message
     * @param cause the cause of this exception occurring
     */
    public LootParsingException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
