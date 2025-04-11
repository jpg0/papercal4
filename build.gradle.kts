group = "com.trillica"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    java
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

configurations.all {
    exclude(group ="androidx.compose.ui", module = "ui-graphics-jvmstubs")
    exclude(group ="androidx.compose.ui", module = "ui-jvmstubs")
    exclude(group ="androidx.compose.ui", module = "ui-text-jvmstubs")
    exclude(group ="androidx.compose.ui", module = "ui-util-jvmstubs")
    exclude(group ="androidx.compose.foundation", module = "foundation-layout-jvmstubs")
    exclude(group ="androidx.compose.foundation", module = "foundation-jvmstubs")
    exclude(group = "androidx.compose.runtime", module = "runtime-jvmstubs")
}

val osName = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
val targetArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64", "arm" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val target = "${targetOs}-${targetArch}"

val composeVersion = "1.7.3"

dependencies {
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("org.jetbrains.compose:compose-full:$composeVersion")
    implementation("net.sf.biweekly:biweekly:0.6.8")
    runtimeOnly("org.jetbrains.skiko:skiko-awt-runtime-$target:0.8.15")
    implementation("org.jetbrains.compose.ui:ui-test-junit4-desktop:$composeVersion")
}

task("execute", JavaExec::class) {
    group = "application"
    mainClass = "com.trillica.MainKt"
    classpath = sourceSets["main"].runtimeClasspath
    if(System.getProperty("os.arch") == "arm") {
        systemProperties = mapOf("os.arch" to "aarch64")
    }
}