import java.util.Properties
import org.gradle.api.GradleException
import org.gradle.api.tasks.Exec

plugins {
    id("com.android.application")
}

val rustWorkspace = rootProject.file("native")
val rustJniLibs = layout.buildDirectory.dir("generated/rustJniLibs")
val nativeNdkVersion = "30.0.16248370"
val minimumSdkApi = "23"

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.isFile) {
    localPropertiesFile.inputStream().use(localProperties::load)
}

val androidSdkPath = providers.environmentVariable("ANDROID_SDK_ROOT").orNull
    ?: providers.environmentVariable("ANDROID_HOME").orNull
    ?: localProperties.getProperty("sdk.dir")

val androidNdkPath = providers.environmentVariable("ANDROID_NDK_HOME").orNull
    ?: androidSdkPath?.let { it + "/ndk/" + nativeNdkVersion }

android {
    namespace = "com.snowgem.kbai"
    compileSdk = 36
    buildToolsVersion = "36.0.0"
    ndkVersion = nativeNdkVersion

    defaultConfig {
        applicationId = "com.snowgem.kbai"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-alpha.1"
    }

    sourceSets.getByName("main").jniLibs.directories.add(rustJniLibs.get().asFile.absolutePath)

    // AGP 9 provides built-in Kotlin support. The Kotlin JVM target follows
    // compileOptions.targetCompatibility; a separate Kotlin Android plugin
    // must not be applied with AGP 9's built-in Kotlin support.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

tasks.register<Exec>("buildRustAndroid") {
    group = "build"
    description = "Build the Rust JNI library for ARM64 Android devices and x86_64 emulators."

    inputs.files(fileTree(rustWorkspace) {
        include("Cargo.toml")
        include("Cargo.lock")
        include("crates/**/Cargo.toml")
        include("crates/**/*.rs")
    })
    inputs.property("androidNdkVersion", nativeNdkVersion)
    inputs.property("androidTargets", "arm64-v8a,x86_64")
    outputs.dir(rustJniLibs)

    doFirst {
        val ndkDirectory = androidNdkPath?.let { file(it) }
        if (ndkDirectory == null || !ndkDirectory.isDirectory) {
            throw GradleException(
                "Android NDK " + nativeNdkVersion +
                    " was not found. Install it with Android SDK Manager or set ANDROID_NDK_HOME."
            )
        }

        environment("ANDROID_NDK_HOME", ndkDirectory.absolutePath)
        environment("CARGO_NDK_PLATFORM", minimumSdkApi)
        rustJniLibs.get().asFile.mkdirs()
    }

    commandLine(
        "cargo",
        "ndk",
        "-t", "arm64-v8a",
        "-t", "x86_64",
        "-o", rustJniLibs.get().asFile.absolutePath,
        "build",
        "--release",
        "--manifest-path", rustWorkspace.resolve("Cargo.toml").absolutePath
    )
}

tasks.named("preBuild").configure {
    dependsOn("buildRustAndroid")
}
