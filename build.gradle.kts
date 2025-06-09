plugins {
    java
    id("org.beryx.runtime") version "1.13.1"
    id("gg.jte.gradle") version("3.1.12")
}

group = "com.tractionrec"
version = "1.11.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.jetbrains:annotations:20.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.openjfx:javafx-swing:18.0.1")
    implementation("com.formdev:flatlaf:2.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.13.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("gg.jte:jte:2.0.2")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("org.apache.commons:commons-io:1.3.2")
    implementation("com.ibm.icu:icu4j:74.2")
    implementation("com.jcabi:jcabi-manifests:1.1")
    implementation("com.github.lgooddatepicker:LGoodDatePicker:11.2.1")
}

jte {
    precompile()
    contentType.set(gg.jte.ContentType.Plain)
}

tasks.jar {
    dependsOn(tasks.precompileJte)
    from(fileTree("jte-classes") {
        include("**/*.class")
    })
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
    applicationDefaultJvmArgs = listOf("-Djdk.httpclient.connectionPoolSize=10")
}

runtime {
    addModules(
            "java.desktop",
            "java.compiler",
            "java.scripting",
            "java.logging",
            "java.net.http",
            "java.xml",
            "jdk.crypto.ec"
    )
    targetPlatform("mac-aarch64") {
        setJdkHome(
                jdkDownload("https://corretto.aws/downloads/latest/amazon-corretto-21-aarch64-macos-jdk.tar.gz")
        )
    }
    targetPlatform("mac-x64") {
        setJdkHome(
                jdkDownload("https://corretto.aws/downloads/latest/amazon-corretto-21-x64-macos-jdk.tar.gz")
        )
    }
    targetPlatform("win") {
        setJdkHome(
                jdkDownload("https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.zip")
        )
    }
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