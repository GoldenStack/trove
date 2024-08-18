plugins {
    id("java-library")
    id("maven-publish")
}

group = "net.goldenstack.loot"
version = "2.1"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains:annotations:24.0.1")
    api("org.spongepowered:configurate-core:4.1.2")

    val minestom = "net.minestom:minestom-snapshots:f8b6eb0d0b"
    val configurate = "org.spongepowered:configurate-gson:4.1.2"

    compileOnly(minestom)
    compileOnly(configurate)

    testImplementation(minestom)
    testImplementation(configurate)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
