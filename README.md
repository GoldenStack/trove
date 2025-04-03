# trove

Trove is a loot table library for [Minestom](https://github.com/Minestom/Minestom/). It implements
[nearly every](#completeness) feature from vanilla Minecraft.

---

## Install

To install, simply add the library via Maven Central:

``` kts
repositories {
    mavenCentral()
}

dependencies {
    implementation 'net.minestom:minestom-snapshots:<version>'
    
    implementation 'net.goldenstack:trove:<version>'
}
```
Make sure to include Minestom, or else Trove won't work.

---

## Usage

###  Setup

Trove is designed to be extremely simple to use.

To obtain a `Map<NamespaceID, LootTable>`, simply call `Trove.readTables(Path.of("path_to_loot_tables"))`. Trove will
automatically parse out the loot table hierarchy and include it in the table IDs.

### Generation
Loot generation is very simple as well. Calling `LootTable#generate(LootContext)` returns a list of items.

If you're implementing block drops, just call `LootTable#blockDrop(LootContext, Instance, Point)`. If you're
implementing entity drops, call `LootTable#drop(LootContext, Instance, Point)`.

TODO: Improve the docs. For now, this might address some concerns:

https://gist.github.com/RealMangorage/d295f217a988dc0f9a996064e54ce6c8

---

## Completeness

TODO

---

## Contributing

Feel free to open a PR or an issue.

Before starting large PRs, make sure to check that it's actually needed; try asking a maintainer.

By contributing to the Trove project you agree that the entirety of your contribution is licensed identically to Trove,
which is currently under the MIT license.

---

## License

This project is licensed under the [MIT](LICENSE) license.
