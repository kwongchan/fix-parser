plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.3"
}

group = "io.github.kwongchan.fixparser"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.6.1")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    jmh("org.quickfixj:quickfixj-core:2.3.2")
    jmh("org.quickfixj:quickfixj-messages-all:2.3.2")
}

tasks.test {
    useJUnitPlatform()
}