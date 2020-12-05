plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("kapt") version "1.4.20"
    id("com.github.johnrengelman.shadow") version("5.2.0")
}

group = "de.stjj"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

val exposedVersion = "0.28.1"
val joobyVersion = "2.9.4"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.jooby:jooby:$joobyVersion")
    implementation("io.jooby:jooby-jetty:$joobyVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.google.guava:guava:30.0-jre")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.moshi:moshi-kotlin:1.11.0")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("mysql:mysql-connector-java:8.0.22")
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("org.apache.tika:tika-core:1.25")
}

tasks {
    compileKotlin {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

    shadowJar {
        archiveFileName.set("stjj-backend.jar")

        manifest {
            attributes("Main-Class" to "de.stjj.backend.MainKt")
        }
    }
}
