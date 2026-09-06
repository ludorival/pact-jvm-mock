import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.git.version)
    id("maven-publish")
}

@Suppress("UNCHECKED_CAST")
val gitVersion = extra["gitVersion"] as groovy.lang.Closure<String>

group = "io.github.ludorival"
version = gitVersion().replace(".dirty", "-SNAPSHOT")

subprojects {
    group = rootProject.group
    version = rootProject.version
    
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jreleaser")
        plugin("maven-publish")
    }

    repositories {
        mavenCentral()
    }

    java {
        withJavadocJar()
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.named<Test>("test") {
        useJUnitPlatform {
            includeTags("!contract-test")
        }
    }
    tasks.register<Test>("contractTest") {
        description = "Runs contract tests"
        group = "verification"
        val testSourceSet = sourceSets.test.get()
        testClassesDirs = testSourceSet.output.classesDirs
        classpath = testSourceSet.runtimeClasspath
        shouldRunAfter(tasks.test)
        useJUnitPlatform {
            includeTags("contract-test")
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
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
                url = uri(layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }

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
            mkdir(layout.buildDirectory.dir("jreleaser"))
        }
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}
