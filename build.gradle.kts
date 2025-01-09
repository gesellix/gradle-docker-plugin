plugins {
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.51.0"
  id("net.ossindex.audit") version "0.4.11"
  id("com.gradle.plugin-publish") version "1.3.0" apply false
  id("io.freefair.maven-central.validate-poms") version "8.11"
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

val dependencyVersions = listOf(
  "com.squareup.okio:okio:3.10.2",
  "com.squareup.okio:okio-jvm:3.10.2",
  "org.jetbrains:annotations:26.0.1",
  "org.jetbrains.kotlin:kotlin-reflect:2.1.0",
  "org.jetbrains.kotlin:kotlin-stdlib:2.1.0",
  "org.jetbrains.kotlin:kotlin-stdlib-common:2.1.0",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.0",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0",
)

val dependencyVersionsByGroup = mapOf(
  "org.codehaus.groovy" to "3.0.22",
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

//project.apply("debug.gradle.kts")

// Updating the Gradle Wrapper:
// ./gradlew wrapper --gradle-version=8.12 --distribution-type=bin --gradle-distribution-sha256-sum=7a00d51fb93147819aab76024feece20b6b84e420694101f276be952e08bef03
// https://gradle.org/whats-new/gradle-8/
// releases: https://gradle.org/releases/
// sha265 checksum reference: https://gradle.org/release-checksums/
// verifying the Gradle Wrapper JAR: https://docs.gradle.org/current/userguide/gradle_wrapper.html#wrapper_checksum_verification
tasks.wrapper {
  gradleVersion = "8.12"
  distributionType = Wrapper.DistributionType.BIN
  distributionSha256Sum = "7a00d51fb93147819aab76024feece20b6b84e420694101f276be952e08bef03"
}
