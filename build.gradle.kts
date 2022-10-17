plugins {
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.43.0"
  id("net.ossindex.audit") version "0.4.11"
  id("com.gradle.plugin-publish") version "1.0.0" apply false
  id("io.freefair.maven-central.validate-poms") version "6.5.1"
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val dependencyVersions = listOf(
  "com.squareup.okio:okio:3.2.0",
  "org.jetbrains:annotations:23.0.0",
  "org.jetbrains.kotlin:kotlin-stdlib:1.6.21",
  "org.jetbrains.kotlin:kotlin-stdlib-common:1.6.21",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21",
)

val dependencyVersionsByGroup = mapOf(
  "org.codehaus.groovy" to "3.0.12",
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

fun findProperty(s: String) = project.findProperty(s) as String?

val isSnapshot = project.version == "unspecified"
nexusPublishing {
  repositories {
    if (!isSnapshot) {
      sonatype {
        // 'sonatype' is pre-configured for Sonatype Nexus (OSSRH) which is used for The Central Repository
        stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: findProperty("sonatype.staging.profile.id")) //can reduce execution time by even 10 seconds
        username.set(System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatype.username"))
        password.set(System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatype.password"))
      }
    }
  }
}

project.apply("debug.gradle.kts")
