package dev.goldenstack.loot.provider.number;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonSerializable;
import org.jetbrains.annotations.NotNull;

public interface NumberProvider extends JsonSerializable<NumberProvider> {

    double getDouble(@NotNull LootContext context);

    default int getInt(@NotNull LootContext context){
        return (int) Math.round(getDouble(context));
    }
}
