dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson.module.kotlin)

    testImplementation(project(":pact-jvm-mock-spring"))
    testImplementation(project(":pact-jvm-mock-mockk"))
    testImplementation(project(":pact-jvm-mock-mockito"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlin.tdd)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.pact.provider.junit5)
    testImplementation(libs.pact.provider.spring7)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
