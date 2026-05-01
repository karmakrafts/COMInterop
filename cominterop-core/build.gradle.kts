/*
 * Copyright 2026 Karma Krafts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import dev.karmakrafts.conventions.configureJava
import dev.karmakrafts.conventions.dokka.configureDokka
import dev.karmakrafts.conventions.kotlin.defaultCompilerOptions
import dev.karmakrafts.conventions.setProjectInfo
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    signing
    `maven-publish`
}

configureJava(libs.versions.java)

configureDokka {
    withKotlin()
}

kotlin {
    defaultCompilerOptions()
    withSourcesJar()
    mingwX64()
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

tasks {
    withType<KotlinJvmTest>().configureEach {
        jvmArgs("-Xms2G", "-Xmx2G")
    }
}

publishing {
    setProjectInfo(
        name = "COMInterop Core",
        description = "Basic COM runtime for Kotlin/Native based on CInterop",
        url = "https://git.karmakrafts.dev/kk/cominterop"
    )
}