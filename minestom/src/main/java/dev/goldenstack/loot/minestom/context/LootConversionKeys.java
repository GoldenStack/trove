package dev.goldenstack.loot.minestom.context;

import dev.goldenstack.loot.context.LootContext;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Holds loot conversion keys often used within this module.
 */
public class LootConversionKeys {
    private LootConversionKeys() {}

    /**
     * Stores loot context key groups that can potentially be used to assure keys in loot contexts.
     */
    public static final @NotNull LootContext.Key<Map<String, LootContextKeyGroup>> CONTEXT_KEYS = new LootContext.Key<>("possible_loot_context_keys", new TypeToken<>(){});

}
