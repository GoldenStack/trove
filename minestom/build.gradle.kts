
repositories {
    maven(url = "https://repo.spongepowered.org/maven")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":core"))

    implementation("com.github.Minestom:Minestom:c5047b8037")

    implementation("org.spongepowered:configurate-gson:4.1.2")

}
