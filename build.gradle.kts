plugins {
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.52.0"
  id("net.ossindex.audit") version "0.4.11"
  id("com.gradle.plugin-publish") version "1.3.1" apply false
  id("io.freefair.maven-central.validate-poms") version "8.13.1"
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

val dependencyVersions = listOf(
  "com.squareup.okio:okio:3.10.2",
  "com.squareup.okio:okio-jvm:3.10.2",
  "org.jetbrains:annotations:26.0.2",
  "org.jetbrains.kotlin:kotlin-reflect:2.1.0",
  "org.jetbrains.kotlin:kotlin-stdlib:2.1.0",
  "org.jetbrains.kotlin:kotlin-stdlib-common:2.1.0",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.0",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0",
)

val dependencyVersionsByGroup = mapOf(
  "org.codehaus.groovy" to libs.versions.groovy3.get(),
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
        nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
        snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
      }
    }
  }
}

//project.apply("debug.gradle.kts")

// Updating the Gradle Wrapper:
// ./gradlew wrapper --gradle-version=8.14 --distribution-type=bin --gradle-distribution-sha256-sum=61ad310d3c7d3e5da131b76bbf22b5a4c0786e9d892dae8c1658d4b484de3caa
// https://gradle.org/whats-new/gradle-8/
// releases: https://gradle.org/releases/
// sha265 checksum reference: https://gradle.org/release-checksums/
// verifying the Gradle Wrapper JAR: https://docs.gradle.org/current/userguide/gradle_wrapper.html#wrapper_checksum_verification
tasks.wrapper {
  gradleVersion = "8.14"
  distributionType = Wrapper.DistributionType.BIN
  distributionSha256Sum = "61ad310d3c7d3e5da131b76bbf22b5a4c0786e9d892dae8c1658d4b484de3caa"
}
