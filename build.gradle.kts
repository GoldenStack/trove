import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "net.goldenstack.trove"
version = "3.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains:annotations:24.0.1")

    val minestom = "net.minestom:minestom-snapshots:e94aaed297"

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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("net.goldenstack", "trove", version.toString())

    pom {
        name.set("trove")
        description.set("Loot table parser and evaluator for Minestom")
        url.set("https://github.com/goldenstack/trove")
        licenses {
            license {
                name.set("MIT")
                url.set("https://github.com/goldenstack/trove/blob/master/LICENSE")
            }
        }
        developers {
            developer {
                id.set("goldenstack")
                name.set("GoldenStack")
                email.set("git@goldenstack.net")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/goldenstack/trove.git")
            developerConnection.set("scm:git:git@github.com:goldenstack/trove.git")
            url.set("https://github.com/goldenstack/trove")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
