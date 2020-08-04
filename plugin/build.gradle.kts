import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.internal.plugins.DslObject

project.extra.set("bintrayDryRun", false)

buildscript {
    repositories {
        mavenLocal()
        jcenter()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    groovy
    `java-gradle-plugin`
    maven
    `maven-publish`
    id("com.github.ben-manes.versions")
    id("net.ossindex.audit")
    id("com.jfrog.bintray")
    id("com.gradle.plugin-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
}

dependencies {
    compile(gradleApi())
    compile(localGroovy())

    compile("de.gesellix:docker-client:2020-06-19T16-12-55")

    testCompile("org.spockframework:spock-core:1.3-groovy-2.5")
    testCompile("cglib:cglib-nodep:3.3.0")

    // see https://docs.gradle.org/current/userguide/test_kit.html
    testCompile(gradleTestKit())
}

tasks {
    withType(Test::class.java) {
        useJUnit()
    }

    bintrayUpload {
        dependsOn("build")
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts {
    add("archives", sourcesJar.get())
}

tasks.install {
    DslObject(repositories)
            .convention
            .getPlugin<MavenRepositoryHandlerConvention>()
            .mavenInstaller {
                pom {
                    groupId = "de.gesellix"
                    artifactId = "gradle-docker-plugin"
                    version = "local"
                }
            }
}

val publicationName = "gradleDockerPlugin"
publishing {
    publications {
        register(publicationName, MavenPublication::class) {
            groupId = "de.gesellix"
            artifactId = "gradle-docker-plugin"
            version = rootProject.extra["artifactVersion"] as String
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}

fun findProperty(s: String) = project.findProperty(s) as String?

bintray {
    user = System.getenv()["BINTRAY_USER"] ?: findProperty("bintray.user")
    key = System.getenv()["BINTRAY_API_KEY"] ?: findProperty("bintray.key")
    setPublications(publicationName)
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "docker-utils"
        name = "gradle-docker-plugin"
        desc = "A Docker plugin for Gradle"
        setLicenses("Apache-2.0")
        setLabels("docker", "gradle", "remote api", "plugin")
        websiteUrl = "https://github.com/gesellix/gradle-docker-plugin"
        issueTrackerUrl = "https://github.com/gesellix/gradle-docker-plugin/issues"
        vcsUrl = "https://github.com/gesellix/gradle-docker-plugin.git"
        attributes = mapOf("gradle-plugin" to "de.gesellix.docker:de.gesellix:gradle-docker-plugin")
        version.name = rootProject.extra["artifactVersion"] as String
        version.attributes = mapOf("gradle-plugin" to "de.gesellix.docker:de.gesellix:gradle-docker-plugin")
    })
    dryRun = project.extra["bintrayDryRun"] as Boolean
}

pluginBundle {
    website = "https://github.com/gesellix/gradle-docker-plugin"
    vcsUrl = "https://github.com/gesellix/gradle-docker-plugin.git"
    description = "A Docker plugin for Gradle"
    tags = listOf("docker", "gradle", "remote api", "plugin")

    plugins {
        register(publicationName) {
            id = "de.gesellix.docker"
            displayName = "Gradle Docker plugin"
            version = rootProject.extra["artifactVersion"] as String
        }
    }

    mavenCoordinates {
        groupId = "de.gesellix"
        artifactId = "gradle-docker-plugin"
        version = rootProject.extra["artifactVersion"] as String
    }
}
