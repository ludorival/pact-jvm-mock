// Plugin imports and declarations
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jreleaser") version "1.15.0"
    id("com.palantir.git-version") version "3.1.0"
}

// Root project configuration
val gitVersion: groovy.lang.Closure<String> by extra

group = "io.github.ludorival"
version = gitVersion().replace(".dirty", "-SNAPSHOT")

// Common configuration for all subprojects
subprojects {
    group = rootProject.group
    version = rootProject.version
    
    apply {
        plugin("kotlin")
    }

    repositories {
        mavenCentral()
    }

    // Java configuration
    java {
        withJavadocJar()
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_17
    }

    // Task configurations
    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    tasks.jar.configure {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
        enabled = true
    }

    // JReleaser configuration
    jreleaser {
        gitRootSearch.set(true)
        project {
            name.set(this@subprojects.name)
            authors.set(listOf("Ludovic Dorival"))
            license.set("The Apache Software License, Version 2.0")
            links {
                homepage.set("https://github.com/ludorival/pact-jvm-mock")
                bugTracker.set("https://github.com/ludorival/pact-jvm-mock/issues")
            }
            inceptionYear.set("2024")
        }

        signing {
            active.set(Active.ALWAYS)
            armored.set(true)
        }

        deploy {
            maven {
                mavenCentral {
                    create("sonatype") {
                        password.set(System.getenv("MAVEN_CENTRAL_PASSWORD"))
                        username.set(System.getenv("MAVEN_CENTRAL_USERNAME"))
                        active.set(Active.ALWAYS)
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepository("target/staging-deploy")
                    }
                }
            }
        }
    }
}

// Repository configuration for all projects
allprojects {
    repositories {
        mavenCentral()
    }
}

// Individual project dependencies
project(":pact-jvm-mockk-core") {
    dependencies {
        compileOnly("io.mockk:mockk:1.13.13")
        compileOnly("com.fasterxml.jackson.core:jackson-databind:2.18.1")
        compileOnly("org.slf4j:slf4j-api:2.0.16")
        implementation("org.bitbucket.cowwoc.diff-match-patch:diff-match-patch:1.0")
        implementation(kotlin("stdlib-jdk8"))
    }
}

project(":pact-jvm-mockk-spring") {
    dependencies {
        api(project(":pact-jvm-mockk-core"))
        implementation(kotlin("stdlib-jdk8"))
        compileOnly("org.springframework:spring-web:6.2.0")
        compileOnly("io.mockk:mockk:1.13.13")
        compileOnly("com.fasterxml.jackson.core:jackson-databind:2.18.1")

        testImplementation("io.mockk:mockk:1.13.13")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
        testImplementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")
        testImplementation("io.github.ludorival:kotlin-tdd:2.0.4.beta")
        testImplementation("org.springframework:spring-web:6.2.0")
    }
}
