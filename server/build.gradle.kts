plugins {
    id("org.springframework.boot") version "2.3.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.openapi.generator") version "4.3.1"
    id("com.google.cloud.tools.jib") version "2.6.0"
    id("org.sonarqube") version "3.0"
    id("io.freefair.lombok") version "5.2.1"
    jacoco
    java
}

group = "com.github.joern.kalz"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.springfox:springfox-swagger2:2.8.0")
    implementation("org.openapitools:jackson-databind-nullable:0.1.0")
    implementation("org.hibernate:hibernate-validator:6.1.5.Final")
    implementation("javax.validation:validation-api")
    implementation("org.apache.httpcomponents:httpclient")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")
    runtimeOnly(project(":client"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/openapi.yaml")
    apiPackage.set("com.github.joern.kalz.doubleentry.generated.api")
    modelPackage.set("com.github.joern.kalz.doubleentry.generated.model")
    outputDir.set("$buildDir/generated/openapi")
    modelNamePrefix.set("Api")
    configOptions.set(mapOf(
            "dateLibrary" to "java8",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "performBeanValidation" to "true"
    ))
}

tasks {
    named("compileJava").configure {
        dependsOn(named("openApiGenerate"))
    }
    named("jacocoTestReport").configure {
        dependsOn(named("test"))
    }
}

sourceSets {
    getByName("main") {
        java {
            srcDir("$buildDir/generated/openapi/src/main/java")
        }
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
}