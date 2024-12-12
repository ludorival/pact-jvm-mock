// Plugin imports and declarations
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active
import org.jreleaser.model.Distribution


plugins {
    kotlin("jvm") version "1.9.25"
    id("org.jreleaser") version "1.15.0"
    id("com.palantir.git-version") version "3.1.0"
    id("maven-publish")
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
        plugin("org.jreleaser")
        plugin("maven-publish")
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
        useJUnitPlatform {
            // Run non-contract tests first
            includeTags("!contract-test")
        }
    }
    tasks.register<Test>("contractTest") {
        description = "Runs contract tests"
        group = "verification"
        useJUnitPlatform {
            includeTags("contract-test")
        }
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

    publishing {
        publications {
            create<MavenPublication>("main") {
                from(this@subprojects.components.getByName("java"))
                pom {
                    name.set("Pact JVM Mock (${this@subprojects.name})")
                    description.set("Pact JVM Mock - Leverage existing Mocks (${this@subprojects.name})")
                    url.set("https://github.com/ludorival/pact-jvm-mock")
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("ludorival")
                            name.set("Ludovic Dorival")
                            email.set("ludorival@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/ludorival/pact-jvm-mock.git")
                        developerConnection.set("scm:git:ssh://github.com/ludorival/pact-jvm-mock.git")
                        url.set("https://github.com/ludorival/pact-jvm-mock")
                    }
                }
            }
        }
        repositories {
            maven {
                url = uri("$buildDir/staging-deploy")
            }
        }
    }

    // JReleaser configuration
    jreleaser {
        gitRootSearch.set(true)
        project {
            name.set(this@subprojects.name)
            description.set("Pact JVM Mock - Leverage existing Mocks (${this@subprojects.name})")
            copyright.set("Copyright 2023 Ludovic Dorival")
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
                        applyMavenCentralRules.set(true)
                        password.set(System.getenv("MAVEN_CENTRAL_PASSWORD"))
                        username.set(System.getenv("MAVEN_CENTRAL_USERNAME"))
                        active.set(Active.ALWAYS)
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepository("build/staging-deploy")
                    }
                }
            }
        }
    }

    tasks.withType<org.jreleaser.gradle.plugin.tasks.JReleaserDeployTask> {
        dependsOn("createJReleaserDir")
    }

    tasks.register("createJReleaserDir") {
        doFirst {
            mkdir("$buildDir/jreleaser")
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
        compileOnly("com.fasterxml.jackson.core:jackson-databind:2.18.2")
        compileOnly("org.slf4j:slf4j-api:2.0.16")
        implementation("org.bitbucket.cowwoc.diff-match-patch:diff-match-patch:1.0")
        implementation(kotlin("stdlib-jdk8"))
    }
}

project(":pact-jvm-mockk-spring") {
    dependencies {
        api(project(":pact-jvm-mockk-core"))
        implementation(kotlin("stdlib-jdk8"))
        compileOnly("org.springframework:spring-web:6.2.1")
        compileOnly("io.mockk:mockk:1.13.13")
        compileOnly("com.fasterxml.jackson.core:jackson-databind:2.18.2")

        testImplementation("io.mockk:mockk:1.13.13")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
        testImplementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
        testImplementation("io.github.ludorival:kotlin-tdd:2.2.0")
        testImplementation("org.springframework:spring-web:6.2.1")
        testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.3")
        testImplementation("org.springframework.boot:spring-boot-starter-web:3.2.3")
        testImplementation("au.com.dius.pact.provider:junit5:4.6.16")
        testImplementation("au.com.dius.pact.provider:junit5spring:4.6.16")
        testImplementation("au.com.dius.pact.provider:spring:4.6.16")
    }
}
