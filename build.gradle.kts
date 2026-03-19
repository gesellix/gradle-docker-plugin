plugins {
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.53.0"
  id("org.sonatype.gradle.plugins.scan") version "3.1.4"
  id("com.gradle.plugin-publish") version "2.1.1" apply false
  id("io.freefair.maven-central.validate-poms") version "9.2.0"
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

val dependencyVersions = listOf(
  "com.squareup.okio:okio:${libs.versions.okio.get()}",
  "com.squareup.okio:okio-jvm:${libs.versions.okio.get()}",
  "org.jetbrains:annotations:26.1.0",
  "org.jetbrains.kotlin:kotlin-reflect:${libs.versions.kotlin.get()}",
  "org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}",
  "org.jetbrains.kotlin:kotlin-stdlib-common:${libs.versions.kotlin.get()}",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${libs.versions.kotlin.get()}",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${libs.versions.kotlin.get()}",
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
// ./gradlew wrapper --gradle-version=9.4.0 --distribution-type=bin --gradle-distribution-sha256-sum=60ea723356d81263e8002fec0fcf9e2b0eee0c0850c7a3d7ab0a63f2ccc601f3
// https://gradle.org/whats-new/gradle-9/
// Releases: https://gradle.org/releases/
// SHA265 checksum reference: https://gradle.org/release-checksums/
// Verifying the Gradle Wrapper JAR:
// https://docs.gradle.org/current/userguide/gradle_wrapper.html#wrapper_checksum_verification
// Configuring the daemon jvm:
// https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:daemon_jvm_criteria
// ./gradlew updateDaemonJvm --jvm-version=17 --jvm-vendor=corretto
tasks.wrapper {
  gradleVersion = "9.4.0"
  distributionType = Wrapper.DistributionType.BIN
  distributionSha256Sum = "60ea723356d81263e8002fec0fcf9e2b0eee0c0850c7a3d7ab0a63f2ccc601f3"
}
