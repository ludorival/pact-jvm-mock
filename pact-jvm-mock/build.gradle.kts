dependencies {
    api(libs.pact.model)
    implementation(libs.pact.support)
    implementation(libs.kotlin.logging)
    implementation(libs.diff.match.patch)
    implementation(kotlin("reflect"))
    compileOnly(libs.junit.jupiter.api)
}
