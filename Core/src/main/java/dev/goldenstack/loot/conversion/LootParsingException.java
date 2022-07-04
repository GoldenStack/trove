package dev.goldenstack.loot.conversion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A generic exception for an issue that has occurred during loot parsing.
 */
public class LootParsingException extends Exception {

    public LootParsingException(@NotNull String message) {
        super(message);
    }

    public LootParsingException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
