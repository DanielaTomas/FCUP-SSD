plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:3.+")
    implementation("io.netty:netty-all:4.1.107.Final")
    implementation("org.bouncycastle:bcprov-jdk15on:+")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.MainClass"
    }
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}



tasks.test {
    useJUnitPlatform()
}