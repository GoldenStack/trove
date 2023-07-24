# trove

[![license](https://img.shields.io/github/license/GoldenStack/trove?style=for-the-badge&color=dd2233)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)
[![javadocs](https://img.shields.io/badge/documentation-javadocs-4d7a97?style=for-the-badge)](https://javadoc.jitpack.io/com/github/GoldenStack/trove/master-SNAPSHOT/javadoc/)

Trove is a versatile loot table library. Although a lot of the base concepts here are similar to Minecraft's loot table
system, Trove is much more flexible, permitting the usage of multiple different loot types (e.g. items and experience).
Plus, it has a convenient API, emphasizes immutable data structures, and supports full serialization and deserialization
of all types supported by Configurate, including JSON and YAML.

The two modules are Core and Minestom. The Core module contains the basic functioning pieces of the library, while
Minestom contains a nearly full implementation for Minecraft's loot tables for Minestom.

---

## Table of Contents
- [Install](#install)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## Install

To install, simply add the library via [JitPack](https://jitpack.io/#GoldenStack/trove/):

Details for how to add this library with other build tools (such as Maven) can be found on the page linked above.
``` kts
repositories {
    ...
    maven(url = "https://jitpack.io")
}

dependencies {
    ...
    implementation("com.github.GoldenStack.trove:MODULE:TAG")
}
```
Just replace MODULE with your desired module and TAG with the desired the desired tag, including the commit tag or just
"-SNAPSHOT".

---

## Usage

###  Setup

This setup currently only explains how to set up the Minestom module.

You can use the TroveMinestom class for a very easy setup. Just provide a folder path, and it will recursively parse
every JSON file inside it.

``` java
Path lootTableFolder = ...; // Replace with the path to the folder of loot tables

LootConversionContext context = LootConversionContext.builder()
        .loader(TroveMinestom.DEFAULT_LOADER)
        .with(LootConversionKeys.CONTEXT_KEYS, TroveMinestom.STANDARD_GROUPS)
        .with(LootContextKeys.VANILLA_INTERFACE, TroveMinestom.DEFAULT_INTERFACE)
        .build();

var tableRegistry = TroveMinestom.readTables(lootTableFolder, context);
```
Each table will be stored via a NamespaceID in the tables object. For example, if the parsed loot table has the path
"blocks/barrel.json" relative to the loot table folder, its ID will be `minecraft:blocks/barrel`.


### Generation
Actual loot generation is fairly simple - you just need to call `LootTable#generate(LootContext)`.
Here's an example that uses the `tableRegistry` variable from the last code snippet:

``` java
LootTable table = tableRegistry.getTable(NamespaceID.from("minecraft:blocks/stone"));

// You can use the LootContextKeys class t
LootGenerationContext context = LootGenerationContext.builder()
        .random(...) // Random instance here
        .with(..., ...) // Loot context key and value here
        .build();

// Generate the loot
LootBatch loot = table.generate(context);
```
To process loot, you could simply call `LootBatch#items` and handle the items, but you can also use a `LootProcessor` to
make this processing easier. For example:
``` java
var processor = LootProcessor.processClass(ItemStack.class, item -> {
    // Perform some arbitrary action with the item
});
```

You can also handle multiple classes at once, or even use a custom predicate:

``` java
var processor = LootProcessor.builder()
        .processClass(ItemStack.class, item -> {
            // Perform some calculation
        }).processClass(String.class, string -> {
            // Perform another calculation
        }).process(object -> true, object -> {
            // Perform some calculation with the object
        }).build();
```

Then, just handle the `LootBatch` that was generated previously:

``` java
processor.processBatch(loot);
```

---

## Contributing

Feel free to open a PR or an issue.

Before starting large PRs, make sure to check that it's actually needed; try asking a maintainer.

By contributing to the Trove project you agree that the entirety of your contribution is licensed identically to Trove,
which is currently under the MIT license.

---

## License

This project is licensed under the [MIT](../LICENSE) license.