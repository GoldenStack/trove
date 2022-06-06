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
thread-safe. Additionally, everything else should be synchronized, so there should be fewer issues there.

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

If you want to use this library, you will first have to set up your `ImmuTables` instance.

The only information that is assigned through the builder and is immutable after is each element name, representing
which key in each `JsonObject` is used to determine which type it is and which deserializer to use.

The following code represents a pretty bare-bones way to create a working loader:

``` java
// Create the basic loader with element names
ImmuTables loader = ImmuTables.builder().setDefaultValues().build();

// Register all of the number providers, conditions, functions, and entries 
ImmuTables.Builder.setupNumberProviderManager(loader.getNumberProviderManager());
ImmuTables.Builder.setupLootConditionManager(loader.getLootConditionManager());
ImmuTables.Builder.setupLootFunctionManager(loader.getLootFunctionManager());
ImmuTables.Builder.setupLootEntryManager(loader.getLootEntryManager());

// Register the default loot parameter groups to this loader
LootParameterGroup.addDefaults(loader);
```

After that, your code is ready to use!

To read a loot table, you can try this:

``` java
ImmuTables loader = ...; // Create this builder somewhere

JsonObject object = ...; // Initialize this JsonObject to something that should be read as a loot table

LootTable table = LootTable.deserialize(object, loader);
```

This loot table can be used to generate loot by providing it a `LootContext`:

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
It's pretty simple to add your own implementations of any of the classes here. Unless you need to convert your class to
or from JSON, you don't need to register it anywhere; you can just add it to somewhere that it could go.

If, however, you do need to deal with JSON, it's pretty simple to do it: just get, from an ImmuTables instance, the
manager with the right type, and then register a `JsonLootConverter` with it. After that, everything else should be
automatically handled when you call the appropriate serialization or deserialization methods.

When implementing `LootFunction`, it is a good idea to extend `ConditionalLootFunction` instead of implementing the
class directly because the class automatically handles adding `LootCondition`s to the function.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)!

Pull requests are very welcomed.

## License

[MIT © GoldenStack](../LICENSE)