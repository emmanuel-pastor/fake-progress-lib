plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.detekt) apply false
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsRootPlugin> {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec> {
        version.set(libs.versions.node.get())
    }
}

tasks.register("checkKotlinBadge") {
    group = "verification"
    description = "Verifies that the Kotlin version badge in README.md matches gradle/libs.versions.toml"

    val readmeFile = layout.projectDirectory.file("README.md").asFile
    val expectedVersion = provider { libs.versions.kotlin.get() }

    inputs.file(readmeFile)
    inputs.property("kotlinVersion", expectedVersion)

    doLast {
        val version = expectedVersion.get()
        val readmeText = readmeFile.readText()
        val expectedBadge = "kotlin-$version-blue"
        if (!readmeText.contains(expectedBadge)) {
            error("README.md Kotlin version badge is out of sync with gradle/libs.versions.toml (expected $expectedBadge).")
        }
    }
}

tasks.matching { it.name == "check" }.configureEach {
    dependsOn("checkKotlinBadge")
}
