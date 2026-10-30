import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test

plugins {
    java
    id("org.springframework.boot") version "4.0.8" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

group = "matchuri"
version = "0.0.1-SNAPSHOT"
description = "Matchuri modular monolith and regression test suite"

allprojects {
    group = rootProject.group
    version = rootProject.version
    repositories { mavenCentral() }
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-parameters")
    }
}
dependencyManagement { imports { mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.8") } }
subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
    extensions.configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports { mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.8") }
    }
    extensions.configure<JavaPluginExtension> {
        toolchain { languageVersion = JavaLanguageVersion.of(21) }
    }
    configurations.named("compileOnly") { extendsFrom(configurations.getByName("annotationProcessor")) }
    dependencies {
        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")
        "annotationProcessor"("org.springframework.boot:spring-boot-configuration-processor")
        "annotationProcessor"("io.github.openfeign.querydsl:querydsl-apt:7.7:jpa")
        "annotationProcessor"("jakarta.annotation:jakarta.annotation-api")
        "annotationProcessor"("jakarta.persistence:jakarta.persistence-api")
    }
}
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
dependencies {
    testImplementation(project(":backend-app"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.modulith:spring-modulith-core:2.0.3")
    testImplementation("io.jsonwebtoken:jjwt-api:0.12.7")
    testImplementation("software.amazon.awssdk:s3:2.25.30")
    testImplementation("net.ttddyy:datasource-proxy:1.10")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.docker.compose.enabled", "false")
}
val fastTest by tasks.registering(Test::class) {
    description = "Runs policy, service, and module boundary tests without Spring contexts."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    exclude("**/*IntegrationTest*.class", "**/*RepositoryTest*.class",
        "**/*SecurityConfigTest*.class", "**/GlobalExceptionHandlerTest*.class")
}
tasks.register<JavaExec>("auditMenuSeedCoverage") {
    description = "Reports reference menu seed coverage using the personal recommendation algorithm."
    group = "verification"
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("matchuri.backend.application.seed.coverage.MenuSeedCoverageAudit")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
    workingDir = projectDir
}

tasks.jar { enabled = false }
tasks.named("build") { dependsOn(subprojects.map { "${it.path}:build" }) }
tasks.named("clean") { dependsOn(subprojects.map { "${it.path}:clean" }) }
tasks.register("bootJar") { group = "build"; dependsOn(":backend-app:bootJar") }
tasks.register("bootRun") { group = "application"; dependsOn(":backend-app:bootRun") }
