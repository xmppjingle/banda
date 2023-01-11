import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.asciidoctor.convert") version "1.5.8"
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
}

group = "com.xmppjingle"
version = "0.0.2-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

extra["snippetsDir"] = file("build/generated-snippets")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-data")
    implementation("org.springframework:spring-aop")
    implementation("org.aspectj:aspectjrt")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.springfox:springfox-boot-starter:3.0.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.spark:spark-core_2.12:3.3.1")

    implementation("com.redislabs:spark-redis_2.12:3.1.0")

    implementation("io.lettuce:lettuce-core:6.2.2.RELEASE")
    implementation("com.google.code.gson:gson:2.10")

    implementation("com.squareup.okhttp3:okhttp:3.14.6")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.7.0")

    implementation("com.redislabs:jredisgraph:2.5.1")
    implementation("io.projectreactor:reactor-core:3.4.0")
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.86.Final:osx-x86_64")
    implementation("io.netty:netty-resolver-dns-native:4.1.86.Final:osx-x86_64")
    implementation("io.netty:netty-transport-native-kqueue:4.1.86.Final:osx-x86_64")

    implementation(kotlin("reflect"))

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("net.ishiis.redis:redis-unit:1.0.3")
    testImplementation("org.redisson:redisson:3.19.0")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

configurations {
    testImplementation.get().isVisible = true
    all {
//        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
        exclude(group = "org.apache.logging.log4j")
        //exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
