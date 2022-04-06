# ImmuTables

[![license](https://img.shields.io/github/license/GoldenStack/ImmuTables?style=for-the-badge&color=dd2233)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)

[![Release](https://jitpack.io/v/GoldenStack/ImmuTables.svg)](https://jitpack.io/#GoldenStack/ImmuTables)

ImmuTables is a rewrite of the Minecraft loot table system, written for Minestom.

This library includes many of the functions, conditions, and entries that default Minecraft has, but it also allows you
to add your own implementations of them through an easy-to-use API.

Because there are no static `ImmuTables` instances, each extension, project, or library has to use its own instance.
This prevents libraries from messing up other libraries.

All classes that can be used to generate loot from a `LootTable` are immutable, so this library is completely
thread-safe. Additionally, most `Map` instances are `ConcurrentHashMap`s, and anything that isn't should have the option
to use a concurrent implementation.

This library depends on the [EnchantmentManager](https://github.com/GoldenStack/EnchantmentManager) library for some
classes.

## Table of Contents
- [Install](#install)
- [Usage](#usage)
- [API](#api)
- [Contributing](#contributing)
- [License](#license)


# Install

To install, simply add the library via [JitPack](https://jitpack.io/#GoldenStack/ImmuTables/-SNAPSHOT):

Gradle -
``` gradle
repositories {
    ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    ...
    implementation 'com.github.GoldenStack:ImmuTables:-SNAPSHOT'
}
```

Maven -
``` xml
<repositories>
    ...
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    ...
    <dependency>
        <groupId>com.github.GoldenStack</groupId>
        <artifactId>ImmuTables</artifactId>
        <version>-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Usage

If you want to use this library, you will first have to set up your `ImmuTables` instance. There are easy-to-use methods
to automatically add the default functions, entries, and conditions to your builder. If you use these methods, you can
still add or remove your own implementations of these, so you're not limited to just the pre-made ones.
``` java
ImmuTables loader = ImmuTables
        // Create the builder
        .builder()
        // Register the default number providers
        .numberProviderBuilder(ImmuTables.Builder::setupNumberProviderBuilder)
        // Register the default loot conditions
        .lootConditionBuilder(ImmuTables.Builder::setupLootConditionManager)
        // Register the default loot functions
        .lootFunctionBuilder(ImmuTables.Builder::setupLootFunctionManager)
        // Register the default loot entries
        .lootEntryBuilder(ImmuTables.Builder::setupLootEntryManager)
        // Modify the default builder for the EnchantmentManager
        .enchantmentManagerBuilder(ImmuTables.Builder::setupEnchantmentManagerBuilder)
    .build();

// Register the default `LootParameterGroup`s to this loader
LootParameterGroup.addDefaults(loader);
```

After that, your code is ready to use!

To read a loot table, you can try this:

``` java
ImmuTables loader = ...; // Create this builder somewhere

JsonObject object = ...; // Initialize this JsonObject to something that should be read as a loot table

LootTable table = LootTable.deserialize(object, loader);
```

This loot table can be used to generate loot by providing it a `LootContext`.

``` java
LootTable table = ...; // Initialize the loot table here

LootContext context = new LootContext();
// Add parameters to the LootContext here

List<ItemStack> loot = table.generateLoot(context);

// Do something with this loot
```

## API
For the simple usages of this library, you can just see the Usage section above.
However, if you wish to add your own implementations, the following text should help you.

#### Adding your changes
Because the default functions are intended to be used as method references, you have to create your own method (or
lambda) and call it yourself. See below:
``` java
ImmuTables loader = ImmuTables.builder()
        .numberProviderBuilder((builder) -> {
            ImmuTables.Builder.setupNumberProviderBuilder(builder);
            builder.putDeserializer(/* The NamespaceID to use*/, /* The deserialization method */)
        })
    .build();
```

When implementing `LootFunction`, it is a good idea to extend `ConditionalLootFunction` instead of implementing the
class directly. This is because the class automatically handles adding `LootCondition`s to the function.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)!

Pull requests are very welcomed.

## License

[MIT © GoldenStack](../LICENSE)