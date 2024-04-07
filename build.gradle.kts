plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.16.1"
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.1")
    pluginName.set("Assertions2Assertj")
    updateSinceUntilBuild.set(false)
    plugins.set(listOf("java"))
}

tasks {
  buildSearchableOptions {
    enabled = false
  }
}

dependencies {
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
}
