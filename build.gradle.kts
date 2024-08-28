plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

group = "net.goldenstack.loot"
version = "3.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains:annotations:24.0.1")

    val minestom = "net.minestom:minestom-snapshots:f8b6eb0d0b"

    compileOnly(minestom)

    testImplementation(minestom)

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

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

            if (System.getenv("SONATYPE_USERNAME") != null) {
                username.set(System.getenv("SONATYPE_USERNAME"))
                password.set(System.getenv("SONATYPE_PASSWORD"))

                println("---\n".repeat(10))
                println(System.getenv("SONATYPE_USERNAME").length)
                println(System.getenv("SONATYPE_PASSWORD").length)
                println("---\n".repeat(10))
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}