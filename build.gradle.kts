plugins {
    java
    application
    id("org.gretty") version "5.0.1"
}

group = "com.paias"
version = "0.0.1-SNAPSHOT"
description = "Flight search app"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.paias.air.AirWebApplication")
}
tasks.jar {
    manifest.attributes["Main-Class"] = "com.paias.air.AirWebApplication"
}


dependencies {
    implementation(platform("org.springframework:spring-framework-bom:7.0.2"))
    implementation("org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE")
    implementation("org.springframework:spring-webmvc")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("org.springframework.data:spring-data-neo4j:8.0.1")
    implementation("eu.michael-simons.neo4j:neo4j-migrations:3.2.0")
    implementation("org.apache.commons:commons-csv:1.14.1")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    runtimeOnly("com.fasterxml.jackson.module:jackson-modules-java8:2.20.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    compileOnly("org.projectlombok:lombok:1.18.42")
    implementation("org.slf4j:slf4j-api:2.0.9")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("org.eclipse.jetty:jetty-server:11.0.26")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.26")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
    testImplementation("org.springframework:spring-test")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
    testImplementation("com.github.tomakehurst:wiremock:3.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.1")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    jvmArgs(
        "-Xms512m",
        "-Xmx6g",
        "-XX:+HeapDumpOnOutOfMemoryError"
    )
}

tasks.test {
    maxHeapSize = "8g"
    jvmArgs = listOf("-Xms512m", "-Xmx8g", "-XX:+HeapDumpOnOutOfMemoryError")
}


tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = "com.paias.air.AirWebApplication"
    }
}

tasks.named("build") {
    dependsOn("fatJar")
}