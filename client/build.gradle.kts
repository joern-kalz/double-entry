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

val buildAngular by tasks.registering(NpmTask::class) {
    dependsOn(tasks.named("openApiGenerate"))
    setArgs(listOf("--prefix", "src/main/angular", "run", "build"))
}

tasks {
    named("jar", Jar::class).configure {
        dependsOn(buildAngular)
        from("$buildDir/generated-resources/main")
        into("static/ui")
    }
}
