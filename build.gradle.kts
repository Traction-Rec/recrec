plugins {
    java
    id("org.beryx.runtime") version "1.12.7"
}

group = "com.tractionrec"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:20.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.openjfx:javafx-swing:18.0.1")
    implementation("com.formdev:flatlaf:2.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("gg.jte:jte:2.0.2")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("com.jcabi:jcabi-manifests:1.1")
}

tasks.jar {
    manifest.attributes["Main-Class"] = "com.tractionrec.recrec.RecRecApplication"
    manifest.attributes["Implementation-Version"] = archiveVersion
    manifest.attributes["Element-Express-Production"] = "prod" == project.property("endpoint")
    val dependencies = configurations
            .runtimeClasspath
            .get()
            .map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("recrec-${project.property("endpoint")}")
}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("com.tractionrec.recrec.RecRecApplication")
}

runtime {
    imageDir.set(file("$buildDir/recrec-${project.property("endpoint")}-${version}-image"))
    imageZip.set(file("$buildDir/recrec-${project.property("endpoint")}-${version}.zip"))
}

val endpoints = listOf("test", "prod")

endpoints.forEach {
    val taskName = "runtimeZip${it.capitalize()}"
    tasks.register<GradleBuild>(taskName) {
        startParameter.projectProperties = mapOf("endpoint" to it)
        tasks = listOf("runtimeZip")
    }
}