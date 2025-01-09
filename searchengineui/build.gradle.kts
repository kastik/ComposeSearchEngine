import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}


kotlin {
    jvm("desktop")
    compilerOptions{
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
    }

    sourceSets {
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(compose.desktop.currentOs)
            implementation(project(":searchengine"))
            implementation(libs.androidx.material3.desktop)
            implementation(libs.compose.material.icons.core.desktop)
            implementation(libs.compose.material.icons.extended.desktop)
            runtimeOnly(libs.gradle)
        }

    }

}
compose.desktop {
    application {
        mainClass = "com.kastik.compose_search_engine.MainKt"
        nativeDistributions {
            targetFormats(
                TargetFormat.Exe,
                TargetFormat.Pkg,
                TargetFormat.AppImage)
            packageName = "com.kastik.compose_search_engine"
            packageVersion = "1.0.0"
        }
        buildTypes.release.proguard {
            configurationFiles.from("proguard-rules.pro")
            optimize.set(false)
            this.obfuscate.set(false)
            version =  "7.6.1"
        }
    }

}
