# ImmuTables

[![license](https://img.shields.io/github/license/GoldenStack/ImmuTables?style=for-the-badge&color=dd2233)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)
[![javadocs](https://img.shields.io/badge/documentation-javadocs-4d7a97?style=for-the-badge)](https://javadoc.jitpack.io/com/github/GoldenStack/ImmuTables/master-SNAPSHOT/javadoc/)

ImmuTables is a platform-agnostic loot table library. Its concepts are similar to Minecraft's loot table system, but all
the code is original.

It includes the basis for loot requirements, loot modifiers, loot entries, and other structural elements of loot tables.
It's incredibly easy to add your own implementations of these, and it's even optional (but encouraged) to add JSON
serialization and deserialization.

Because there are no static `ImmuTables` instances, each client of this library must use its own instance, preventing
potential interference.

All classes that can be used to generate loot from a `LootTable` are fully immutable (unless implementations of them
aren't, which is not recommended), so this library should be thread safe; however, builders of these immutable classes
are not synchronized, so care must be taken when working with them.

---

## Table of Contents
- [Install](#install)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## Install

To install, simply add the library via [JitPack](https://jitpack.io/#GoldenStack/ImmuTables/-SNAPSHOT):

Details for how to add this library with other build tools (such as Maven) can be found on the page linked above.
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

---

## Usage

###  Setup
In order to actually use this library, you will almost definitely have to choose your own type for the generic.
However, it's technically optional - the following code segment shows the bare minimum that is required for the code to
compile and run; as demonstrated, you just have to give each builder an element name and provide loot table and loot
pool conversion information.

``` java
ImmuTables<L> loader = ImmuTables.<L>builder()
    .lootEntryBuilder(builder -> {
        builder.keyLocation("key here");
        // Modify the loot entry builder here
    })
    .lootModifierBuilder(builder -> {
        builder.keyLocation("key here");
        // Modify the loot modifier builder here
    })
    .lootRequirementBuilder(builder -> {
        builder.keyLocation("key here");
        // Modify the loot requirement builder here
    })
    .lootNumberBuilder(builder -> {
        builder.keyLocation("key here");
        // Modify the loot number builder here
    })
    .lootPoolConverter(new LootPool.Converter<>())
    .lootTableConverter(new LootTable.Converter<>())
    .build();
```

The customizability required for actual functionality can be reached by using the additional methods in the provided
builder or the ones contained inside it.

### Conversion

Here is a minimal working example of how you convert loot tables to and from JSON:

``` java
// Initialize the loader here
ImmuTables<L> loader = ...;

// Put the element here
JsonElement element = ...;

// Put the context here. The one here is the most barren contex that actually works.
// Additional information can be added to it with LootConversionContext.Builder#addInformation.
LootConversionContext<L> context = LootConversionContext.<L>builder().loader(loader).build();

LootTable<L> table = loader.lootTableConverter().deserialize(element, context);
```

### Generation
Actual loot generation is fairly simple - you just need to call `LootTable#generate(LootContext)`.

``` java
// Initialize the loot table here
LootTable<L> table = ...;

// Create the context here.
// Additional information can be added to it with LootContext.Builder#addInformation.
LootContext context = LootContext.builder().random(new Random()).build();
```

---

## Contributing

Feel free to open a PR or an issue.

Before starting large PRs, make sure to check that it's actually needed; try asking a maintainer.

Before a PR is merged, all contributors must sign the [DCO](https://developercertificate.org/).
We use [cla-assistant](https://github.com/cla-assistant/cla-assistant) to verify this.

---

## License

[MIT © GoldenStack](../LICENSE)