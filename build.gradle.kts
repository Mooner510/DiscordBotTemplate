plugins {
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    // https://mvnrepository.com/artifact/net.dv8tion/JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.20")
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("org.springframework:spring-context:6.1.4")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
}

tasks.test {
    useJUnitPlatform()
}
java {
    version = 17
}
kotlin {
    jvmToolchain(17)
}