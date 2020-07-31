plugins {
    java
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    id("com.github.johnrengelman.shadow") version("5.2.0")
}

val ktorVersion = "1.3.2"
val exposedVersion = "0.25.1"

group = "de.stjj"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.jooby:jooby:2.8.8")
    implementation("io.jooby:jooby-jetty:2.8.8")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.google.guava:guava:29.0-jre")
    implementation("ch.qos.logback:logback-classic:1.2.1")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.3")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("mysql:mysql-connector-java:8.0.20")
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.8.0")
    implementation("org.apache.tika:tika-core:1.24.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    shadowJar {
        archiveFileName.set("stjj-backend.jar")

        manifest {
            attributes("Main-Class" to "de.stjj.backend.MainKt")
        }
    }
}
