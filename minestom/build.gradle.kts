
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":core"))

    val minestom = "com.github.minestom.minestom:Minestom:2cdb3911b0"
    val sponge = "org.spongepowered:configurate-gson:4.1.2"

    compileOnly(minestom)
    compileOnly(sponge)

    testImplementation(minestom)
    testImplementation(sponge)
}
