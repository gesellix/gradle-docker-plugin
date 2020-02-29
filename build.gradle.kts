import java.text.SimpleDateFormat
import java.util.*

rootProject.extra.set("artifactVersion", SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date()))

buildscript {
    repositories {
        mavenLocal()
        jcenter()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.28.0"
    id("net.ossindex.audit") version "0.4.11"
    id("com.jfrog.bintray") version "1.8.4" apply false
    id("com.gradle.plugin-publish") version "0.10.1" apply false
}

val dependencyVersions = listOf(
        "com.squareup.okio:okio:2.4.3",
        "org.jetbrains.kotlin:kotlin-reflect:1.3.61",
        "org.jetbrains.kotlin:kotlin-stdlib:1.3.61",
        "org.jetbrains.kotlin:kotlin-stdlib-common:1.3.61",
        "org.slf4j:slf4j-api:1.7.30"
)

val dependencyVersionsByGroup = mapOf(
        "org.codehaus.groovy" to "2.5.9"
)

subprojects {
    configurations.all {
        resolutionStrategy {
            failOnVersionConflict()
            force(dependencyVersions)
            eachDependency {
                val forcedVersion = dependencyVersionsByGroup[requested.group]
                if (forcedVersion != null) {
                    useVersion(forcedVersion)
                }
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "5.6.4"
        distributionType = Wrapper.DistributionType.ALL
    }
}

project.apply("debug.gradle.kts")
