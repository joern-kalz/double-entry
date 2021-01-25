import com.moowork.gradle.node.npm.NpmTask

plugins {
    java
    id("org.openapi.generator") version "4.3.1"
    id("com.github.node-gradle.node") version "2.2.4"
}

node {
    download = true
    version = "14.8.0"
}

openApiGenerate {
    generatorName.set("typescript-angular")
    inputSpec.set("$rootDir/openapi.yaml")
    outputDir.set("$projectDir/src/main/angular/src/app/generated/openapi")
}

val angularNpmInstall by tasks.registering(NpmTask::class) {
    dependsOn(tasks.named("openApiGenerate"))
    setWorkingDir(file("src/main/angular"))
    setArgs(listOf("install"))
}

val angularNpmBuild by tasks.registering(NpmTask::class) {
    dependsOn(angularNpmInstall)
    setWorkingDir(file("src/main/angular"))
    setArgs(listOf("run", "build"))
}

val angularNpmTest by tasks.registering(NpmTask::class) {
    setWorkingDir(file("src/main/angular"))
    setArgs(listOf("test", "sonar", "--no-watch", "--code-coverage"))
}

val angularNpmSonar by tasks.registering(NpmTask::class) {
    dependsOn(angularNpmTest)
    setWorkingDir(file("src/main/angular"))
    setArgs(listOf(
        "run", "sonar", "--", 
        "-Dsonar.host.url=${System.getProperty("sonar.host.url")}",
        "-Dsonar.login=${System.getProperty("sonar.login")}",
        "-Dsonar.projectVersion=${project.version}"
    ))
}

tasks {
    named("sonarqube").configure {
        dependsOn(angularNpmSonar)
    }
    named("jar", Jar::class).configure {
        dependsOn(angularNpmBuild)
        from("$buildDir/generated-resources/main")
        into("static/ui")
    }
}
