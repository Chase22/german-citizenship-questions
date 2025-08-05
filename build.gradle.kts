import de.undercouch.gradle.tasks.download.Download

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    id("de.undercouch.download") version "5.6.0"
}

group = "com.rewe.digital.gradle"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fleeksoft.ksoup:ksoup:0.2.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

val fetchHtml = tasks.register<Download>("FetchHtml") {
    src((1..10).map {
        "https://www.lebenindeutschland.eu/fragenkatalog/${it}"
    })
    dest(layout.buildDirectory.dir("html"))
    eachFile { name = "${name.removePrefix(".html")}.html" }
}

val processHtml = tasks.register<JavaExec>("ProcessHtml") {
    val inputsDir = fetchHtml.get().dest
    val outputsDir = layout.buildDirectory.dir("questions")

    inputs.dir(inputsDir)
    outputs.dir(outputsDir)

    dependsOn(fetchHtml, tasks.build)

    group = "build"
    description = "Processes the downloaded HTML files to extract questions."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("de.chasenet.citizenship.MainKt")
    args = listOf(
        inputsDir.absolutePath,
        outputsDir.get().asFile.absolutePath
    )
}