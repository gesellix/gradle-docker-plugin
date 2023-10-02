plugins {
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.48.0"
  id("net.ossindex.audit") version "0.4.11"
  id("com.gradle.plugin-publish") version "1.2.1" apply false
  id("io.freefair.maven-central.validate-poms") version "8.3"
  id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

val dependencyVersions = listOf(
  "com.squareup.okio:okio:3.6.0",
  "com.squareup.okio:okio-jvm:3.6.0",
  "org.jetbrains:annotations:24.0.1",
  "org.jetbrains.kotlin:kotlin-reflect:1.9.10",
  "org.jetbrains.kotlin:kotlin-stdlib:1.9.10",
  "org.jetbrains.kotlin:kotlin-stdlib-common:1.9.10",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.10",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10",
)

val dependencyVersionsByGroup = mapOf(
  "org.codehaus.groovy" to "3.0.17",
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

// https://gradle.org/release-checksums/
tasks.wrapper {
  gradleVersion = "8.2"
  distributionType = Wrapper.DistributionType.BIN
  distributionSha256Sum = "38f66cd6eef217b4c35855bb11ea4e9fbc53594ccccb5fb82dfd317ef8c2c5a3"
}
