import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
}

group = "io.github.ludorival"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.mockk:mockk:1.13.4")
    implementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    testImplementation("io.github.ludorival:kotlin-tdd:2.0.4.beta")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

