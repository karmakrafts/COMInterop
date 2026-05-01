# COMInterop

[![](https://git.karmakrafts.dev/kk/cominterop/badges/master/pipeline.svg)](https://git.karmakrafts.dev/kk/cominterop/-/pipelines)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.maven.apache.org%2Fmaven2%2Fdev%2Fkarmakrafts%2Fcominterop%2Fcominterop-core%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/cominterop/-/packages)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdev%2Fkarmakrafts%2Fcominterop%2Fcominterop-core%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/cominterop/-/packages)
[![](https://img.shields.io/badge/2.3.21-blue?logo=kotlin&label=kotlin)](https://kotlinlang.org/)
[![](https://img.shields.io/badge/documentation-black?logo=kotlin)](https://docs.karmakrafts.dev/cominterop-core)

A basic COM runtime for Kotlin/Native based on CInterop.  
This can be used to interop with WinRT/Windows SDK functionality otherwise only available through Microsoft compiler
extensions and C++ code.

### How to use it

First, add the official Maven Central repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

Then add a dependency on the library in your buildscript:

```kotlin
kotlin {
    mingwMain {
        dependencies {
            implementation("dev.karmakrafts.cominterop:cominterop-core:<version>")
        }
    }
}
```