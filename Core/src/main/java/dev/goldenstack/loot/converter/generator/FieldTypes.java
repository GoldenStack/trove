package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.converter.meta.LootConversionManager;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Utility for the creation of various types of fields.
 */
public class FieldTypes {

    /**
     * Creates a field that assumes that any provided configuration node already has a converter for the given type.
     * @param type the type to convert
     * @return a field representing the type
     * @param <T> the type to convert
     */
    public static <T> @NotNull Field<T> implicit(@NotNull Class<T> type) {
        return implicit(TypeToken.get(type));
    }

    /**
     * Creates a field that assumes that any provided configuration node already has a converter for the given type.
     * @param type the type to convert
     * @return a field representing the type
     * @param <T> the type to convert
     */
    public static <T> @NotNull Field<T> implicit(@NotNull TypeToken<T> type) {
        return Field.field(type,
                Utils.createAdditive(
                    (input, result, context) -> result.set(type, input),
                    (input, context) -> input.require(type)
                )
        );
    }

    /**
     * @return a field converting loot conditions
     */
    public static @NotNull Field<LootCondition> condition() {
        return converterOfManager(LootCondition.class, ImmuTables::lootConditionManager);
    }

    /**
     * @return a field converting loot entries
     */
    public static @NotNull Field<LootEntry> entry() {
        return converterOfManager(LootEntry.class, ImmuTables::lootEntryManager);
    }

    /**
     * @return a field converting loot modifiers
     */
    public static @NotNull Field<LootModifier> modifier() {
        return converterOfManager(LootModifier.class, ImmuTables::lootModifierManager);
    }

    /**
     * @return a field converting loot numbers
     */
    public static @NotNull Field<LootNumber> number() {
        return converterOfManager(LootNumber.class, ImmuTables::lootNumberManager);
    }

    private static <T> @NotNull Field<T> converterOfManager(@NotNull Class<T> type, @NotNull Function<ImmuTables, LootConversionManager<T>> getter) {
        return Field.field(type,
                Utils.createAdditive(
                    (input, result, context) -> getter.apply(context.loader()).serialize(input, result, context),
                    (input, context) -> getter.apply(context.loader()).deserialize(input, context)
                )
        );
    }



}
