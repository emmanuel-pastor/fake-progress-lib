import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

group = "io.github.emmanuel-pastor"
version = rootProject.version

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation()

    jvm()
    android {
        namespace = "io.github.emmanuel_pastor.fake.progress"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    // Kotlin/Native — Tier 1
    macosArm64()

    iosArm64()
    iosSimulatorArm64()

    // Kotlin/Native — Tier 2
    linuxX64()
    linuxArm64()

    watchosSimulatorArm64()
    watchosArm64()

    tvosSimulatorArm64()
    tvosArm64()

    // Kotlin/Native — Tier 3
    mingwX64()

    iosX64()
    watchosDeviceArm64()

    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    // Web
    js {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/config/detekt.yml")
}

dependencies {
    detektPlugins(libs.detekt.rules.libraries)
}

kover {
    reports {
        total {
            xml {
                onCheck = true
            }
            verify {
                rule {
                    minBound(80, kotlinx.kover.gradle.plugin.dsl.CoverageUnit.INSTRUCTION)
                }
            }
        }
    }
}

dokka {
    moduleName.set("fake-progress-lib")
    dokkaPublications.configureEach {
        outputDirectory.set(layout.projectDirectory.dir("../docs/kdoc"))
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "fake-progress-lib", version.toString())

    pom {
        name = "Fake progress"
        description = "Adaptive fake progress library for asynchronous tasks with unknown duration"
        inceptionYear = "2026"
        url = "https://github.com/emmanuel-pastor/fake-progress-lib/"
        licenses {
            license {
                name = "XXX"
                url = "YYY"
                distribution = "ZZZ"
            }
        }
        developers {
            developer {
                id = "XXX"
                name = "YYY"
                url = "ZZZ"
            }
        }
        scm {
            url = "XXX"
            connection = "YYY"
            developerConnection = "ZZZ"
        }
    }
}
