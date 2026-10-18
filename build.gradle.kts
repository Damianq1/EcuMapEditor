plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Nowoczesny sposób czyszczenia katalogu build w Gradle
tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
