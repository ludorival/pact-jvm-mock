dependencies {
    api(project(":pact-jvm-mock"))
    compileOnly(libs.spring.web)
    compileOnly(libs.jackson.databind)
}
