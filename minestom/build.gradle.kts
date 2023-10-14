
repositories {
    maven(url = "https://repo.spongepowered.org/maven")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":core"))

    implementation("com.github.minestom.minestom:Minestom:2cdb3911b0")

    implementation("org.spongepowered:configurate-gson:4.1.2")

}
