import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    //id("org.openapi.generator") version "7.0.1"
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // ---------- Swagger ---------- \\
    val springdocStarterVersion = "2.3.0"
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocStarterVersion")

    // ---------- Spring Cloud ---------- \\
    val springCloudVersion = "4.1.0"
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:$springCloudVersion")

    implementation("org.eaxy:eaxy:0.2.2")

    val feignVersion = "13.1"
    implementation("io.github.openfeign:feign-core:$feignVersion")
    implementation("io.github.openfeign:feign-gson:$feignVersion")
    implementation("io.github.openfeign:feign-okhttp:$feignVersion")
    implementation("io.github.openfeign:feign-slf4j:$feignVersion")

    implementation("com.google.guava:guava:33.0.0-jre")

    // ---------- Entur ---------- \\
    implementation("org.entur.logging:common-logging-spring-boot-starter:2.0.1") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-security")
    }

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    validateSpec.set(false)
    inputSpec.set("$rootDir/src/main/resources/OSDM-online-api-v3.0.3.yml")
    generatorName.set("kotlin-spring")
    outputDir.set("${buildDir}/generated-openapi/osdm")
    modelPackage.set("io.osdm")

    typeMappings.set(mapOf("java.time.OffsetDateTime" to "java.time.ZonedDateTime"))
    globalProperties.put("models", "")
    generateApiTests.set(false)
    configOptions.put("gradleBuildFile", "false")
    configOptions.put("collectionType", "list")
    configOptions.put("enumPropertyNaming", "UPPERCASE")
    configOptions.put("useSpringBoot3", "true")

    sourceSets["main"].java.srcDir(file("${buildDir}/generated-openapi/osdm/src/main"))
}

tasks.compileKotlin {
    dependsOn(tasks.getByName("generateOsdmSpec"))
}
