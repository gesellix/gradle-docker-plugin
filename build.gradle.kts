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
    id("com.github.ben-manes.versions") version "0.33.0"
    id("net.ossindex.audit") version "0.4.11"
    id("com.jfrog.bintray") version "1.8.5" apply false
    id("com.gradle.plugin-publish") version "0.12.0" apply false
}

val dependencyVersions = listOf(
        "com.squareup.okio:okio:2.8.0",
        "org.jetbrains.kotlin:kotlin-reflect:1.3.72",
        "org.jetbrains.kotlin:kotlin-stdlib:1.3.72",
        "org.jetbrains.kotlin:kotlin-stdlib-common:1.3.72"
)

val dependencyVersionsByGroup = mapOf(
        "org.codehaus.groovy" to "2.5.13"
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
        gradleVersion = "6.8.2"
        distributionType = Wrapper.DistributionType.ALL
    }
}

project.apply("debug.gradle.kts")
