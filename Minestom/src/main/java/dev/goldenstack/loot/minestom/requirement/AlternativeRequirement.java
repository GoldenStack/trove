package dev.goldenstack.loot.minestom.requirement;

import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.conversion.LootConverter;
import dev.goldenstack.loot.structure.LootRequirement;
import dev.goldenstack.loot.util.JsonUtils;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Assures that at least one of the requirements in {@link #terms()} is true.
 * @param terms the list of requirements to check
 */
public record AlternativeRequirement(@NotNull List<LootRequirement<ItemStack>> terms) implements LootRequirement<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, AlternativeRequirement> CONVERTER = new LootConverter<>("minecraft:alternative", AlternativeRequirement.class) {
        @Override
        public @NotNull AlternativeRequirement deserialize(@NotNull JsonObject json, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            return new AlternativeRequirement(context.loader().lootRequirementManager().deserializeList(JsonUtils.assureJsonArray(json.get("terms"), "terms"), context));
        }

        @Override
        public void serialize(@NotNull AlternativeRequirement input, @NotNull JsonObject result, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            result.add("terms", context.loader().lootRequirementManager().serializeList(input.terms, context));
        }
    };

    public AlternativeRequirement {
        terms = List.copyOf(terms);
    }

    /**
     * @param context the loot context that will be verified
     * @return true if at least one of the requirements in {@link #terms()} returns true
     */
    @Override
    public boolean check(@NotNull LootContext context) {
        return LootRequirement.or(context, terms);
    }
}
