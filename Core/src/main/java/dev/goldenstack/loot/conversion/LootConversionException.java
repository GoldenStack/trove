package dev.goldenstack.loot.conversion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A generic exception for an issue that has occurred during loot conversion.
 */
public class LootConversionException extends Exception {

    /**
     * Creates a new instance from the provided arguments.
     * @param message this exception's error message
     */
    public LootConversionException(@NotNull String message) {
        super(message);
    }

    /**
     * Creates a new instance from the provided arguments.
     * @param message this exception's error message
     * @param cause the cause of this exception occurring
     */
    public LootConversionException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
