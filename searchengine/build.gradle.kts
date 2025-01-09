import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    //id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
}

kotlin {
    compilerOptions{
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
    }
}
dependencies {
    implementation(libs.lucene.core)
    implementation(libs.lucene.codecs)
    implementation(libs.lucene.highlighter)
    implementation(libs.lucene.queryparser)
    implementation(libs.lucene.analysis.common)
    implementation(libs.lucene.analyzers.common)
    implementation(libs.jsoup)
    implementation(libs.stanford.corenlp)
    implementation(variantOf(libs.stanford.corenlp) { classifier("models") })

    //implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.zstd.jni)
    implementation(libs.commons.compress)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotest.runner.junit5.jvm)
    testImplementation(libs.slf4j.api)
    testImplementation(libs.logback.classic)
    testImplementation(libs.mockk)

    runtimeOnly("com.guardsquare:proguard-gradle:7.6.1")


}

tasks.withType<Test>().configureEach {
   useJUnitPlatform()
}

