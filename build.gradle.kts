import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("com.palantir.git-version") version "3.0.0"
}


val gitVersion: groovy.lang.Closure<String> by extra

group = "io.github.ludorival"
version = gitVersion().replace(".dirty", "-SNAPSHOT")
subprojects {
    group = rootProject.group
    version = rootProject.version
    apply {
        plugin("kotlin")
        plugin("maven-publish")
        plugin("signing")
    }

    group = "io.github.ludorival"

    repositories {
        mavenCentral()
    }
    java {
        withJavadocJar()
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
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
                    name.set(this@subprojects.name)
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
                name = "OSSRH"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = findProperty("maven.username")?.toString() ?: System.getenv("OSSRH_USERNAME")
                    password =
                        findProperty("maven.password")?.toString() ?: System.getenv("OSSRH_GPG_SECRET_KEY_PASSWORD")
                }

            }
        }
    }

    signing {
        sign(publishing.publications["main"])
    }


}

allprojects {
    repositories {
        mavenCentral()
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(findProperty("maven.username")?.toString() ?: System.getenv("OSSRH_USERNAME"))
            password.set(findProperty("maven.password")?.toString() ?: System.getenv("OSSRH_GPG_SECRET_KEY_PASSWORD"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

project(":pact-jvm-mockk-core") {
    dependencies {
        compileOnly("io.mockk:mockk:1.13.11")
        compileOnly("com.fasterxml.jackson.core:jackson-databind:2.17.1")
        compileOnly("org.slf4j:slf4j-api:2.0.13")
        implementation("org.bitbucket.cowwoc.diff-match-patch:diff-match-patch:1.0")
        implementation(kotlin("stdlib-jdk8"))
    }
}

project(":pact-jvm-mockk-spring") {
    dependencies {
        api(project(":pact-jvm-mockk-core"))
        implementation(kotlin("stdlib-jdk8"))
        compileOnly("org.springframework:spring-web:5.3.34")
        compileOnly("io.mockk:mockk:1.13.11")
        compileOnly("com.fasterxml.jackson.core:jackson-databind:2.17.1")

        testImplementation("io.mockk:mockk:1.13.11")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
        testImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
        testImplementation("io.github.ludorival:kotlin-tdd:2.0.4.beta")
        testImplementation("org.springframework:spring-web:5.3.34")
    }
}
