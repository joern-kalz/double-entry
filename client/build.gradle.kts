import com.moowork.gradle.node.npm.NpmTask

plugins {
    java
    id("com.moowork.node") version "1.3.1"
}

val buildAngular by tasks.registering(NpmTask::class) {
    setArgs(listOf("--prefix", "src/main/angular", "run", "build"))
}

tasks {
    named("jar", Jar::class).configure {
        dependsOn(buildAngular)
        from("$buildDir/generated-resources/main")
        into("static/ui")
    }
}
