import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    jvm()
    
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.ui.ExperimentalComposeUiApi")
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }
        wasmJsMain.dependencies {
            implementation("io.ktor:ktor-client-js:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-cio:${libs.versions.ktor.get()}")
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${libs.versions.coroutines.get()}")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.antonbutov.aisearch.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ИИ поиск"
            packageVersion = "1.0.0"
            
            macOS {
                bundleID = "com.antonbutov.aisearch"
            }
            
            windows {
                menuGroup = "ИИ поиск"
                upgradeUuid = "18159995-d967-4cd2-8885-77BFA97CFA9F"
            }
        }
    }
}


