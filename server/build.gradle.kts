plugins {
    id("org.springframework.boot") version "2.3.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.openapi.generator") version "4.3.1"
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
    runtimeOnly("com.h2database:h2")
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
    configOptions.set(mapOf(
            "dateLibrary" to "java8",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "performBeanValidation" to "true",
            "useOptional" to "true"
    ))
}

tasks {
    named("compileJava").configure {
        dependsOn(named("openApiGenerate"))
    }
}

sourceSets {
    getByName("main") {
        java {
            srcDir("$buildDir/generated/openapi/src/main/java")
        }
    }
}