plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "the.lyn"
version = "0.0.1-SNAPSHOT"

val exposedVersion = "0.55.0"
val kotestVersion = "5.9.1"
val kotestSpringExtensionVersion = "1.3.0"
val mockkVersion = "1.13.13"
val byteBuddyVersion = "1.15.7"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")

    implementation("com.h2database:h2")

    implementation("net.bytebuddy:byte-buddy:$byteBuddyVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:$kotestSpringExtensionVersion")
    testImplementation("io.mockk:mockk:${mockkVersion}")

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
