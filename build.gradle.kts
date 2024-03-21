import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    id("org.openapi.generator") version "7.3.0"
}

group = "org.entur"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://entur2.jfrog.io/entur2/entur-release-standard/")
        credentials {
            username = "${project.properties["entur_artifactory_user"] ?: System.getenv("ARTIFACTORY_USER")}"
            password = "${project.properties["entur_artifactory_password"] ?: System.getenv("ARTIFACTORY_PASSWORD")}"
        }
    }
}

val springCloudVersion = "2023.0.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")


    // ---------- Swagger ---------- \\
    val springdocStarterVersion = "2.3.0"
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocStarterVersion")

    // ---------- Spring Cloud ---------- \\
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    implementation("org.eaxy:eaxy:0.2.2")

    // ---------- Entur ---------- \\
    implementation("org.entur.logging:common-logging-spring-boot-starter:2.0.1") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-security")
    }

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")



    if (findProperty("entur") == "true") {
        println("entur!")
        // ---------- Entur ------ ---- \\
        val oidcAuthVersion = "3.9.1"
        implementation("org.entur.auth:oauth2-rs-spring-boot-3-web:${oidcAuthVersion}")
        testImplementation("org.entur.auth:oauth2-rs-spring-boot-3-web-test:${oidcAuthVersion}")
        implementation("org.entur.logging:common-logging-spring-boot-starter:2.0.1")
    }

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<GenerateTask>("generateOsdmSpec") {
    val buildPath = layout.buildDirectory.get().asFile.path
    validateSpec.set(false)
    inputSpec.set("$rootDir/src/main/resources/OSDM-online-api-v3.0.3.yml")
    generatorName.set("kotlin-spring")
    outputDir.set("${buildPath}/generated-openapi/osdm")
    modelPackage.set("io.osdm")

    typeMappings.set(mapOf("java.time.OffsetDateTime" to "java.time.ZonedDateTime"))
    globalProperties.put("models", "")
    generateApiTests.set(false)
    configOptions.put("gradleBuildFile", "false")
    configOptions.put("collectionType", "list")
    configOptions.put("enumPropertyNaming", "UPPERCASE")
    configOptions.put("useSpringBoot3", "true")

    sourceSets["main"].java.srcDir(file("${buildPath}/generated-openapi/osdm/src/main"))
}

tasks.register<GenerateTask>("generateOSDMCoverterSpec") {
    validateSpec.set(false)
    inputSpec.set("$rootDir/src/main/resources/OSDM-converter-api-v1.yml")
    generatorName.set("kotlin-spring")

    outputDir.set(layout.buildDirectory.file("generated-openapi/osdm").map { it.asFile.path })
    modelPackage.set("io.osdm")

    typeMappings.set(mapOf("java.time.OffsetDateTime" to "java.time.ZonedDateTime"))
    globalProperties.put("models", "")
    generateApiTests.set(false)
    configOptions.put("gradleBuildFile", "false")
    configOptions.put("collectionType", "list")
    configOptions.put("enumPropertyNaming", "UPPERCASE")
    configOptions.put("useSpringBoot3", "true")

    sourceSets["main"].java.srcDir(layout.buildDirectory.file("generated-openapi/osdm/src/main"))
}

tasks.compileKotlin {
    dependsOn(tasks.getByName("generateOsdmSpec"))
    dependsOn(tasks.getByName("generateOSDMCoverterSpec"))
}
