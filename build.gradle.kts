plugins {
    id("org.openapi.generator") version "4.3.0"
}

openApiGenerate {
    generatorName.set("typescript-angular")
    inputSpec.set("$rootDir/client-server-shared/openapi.yml".toString())
    outputDir.set("$rootDir/angular-client/src/app/server".toString())
}