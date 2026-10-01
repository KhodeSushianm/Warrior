// Plugin versions are declared here once; see gradle/libs.versions.toml for libraries.
// The Kotlin serialization plugin is resolved here too (apply false) so :app does not put a
// second, independently-versioned copy of the Kotlin plugin classpath on the build script.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    kotlin("plugin.serialization") version libs.versions.kotlin.get() apply false
}
