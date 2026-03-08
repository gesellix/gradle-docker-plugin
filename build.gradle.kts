plugins {
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.53.0"
  id("org.sonatype.gradle.plugins.scan") version "3.1.4"
  id("com.gradle.plugin-publish") version "2.0.0" apply false
  id("io.freefair.maven-central.validate-poms") version "9.2.0"
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

val dependencyVersions = listOf(
  "com.squareup.okio:okio:3.16.2",
  "com.squareup.okio:okio-jvm:3.16.2",
  "org.jetbrains:annotations:26.1.0",
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
// ./gradlew wrapper --gradle-version=9.3.0 --distribution-type=bin --gradle-distribution-sha256-sum=0d585f69da091fc5b2beced877feab55a3064d43b8a1d46aeb07996b0915e0e0
// https://gradle.org/whats-new/gradle-9/
// Releases: https://gradle.org/releases/
// SHA265 checksum reference: https://gradle.org/release-checksums/
// Verifying the Gradle Wrapper JAR:
// https://docs.gradle.org/current/userguide/gradle_wrapper.html#wrapper_checksum_verification
// Configuring the daemon jvm:
// https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:daemon_jvm_criteria
// ./gradlew updateDaemonJvm --jvm-version=17 --jvm-vendor=corretto
tasks.wrapper {
  gradleVersion = "9.3.0"
  distributionType = Wrapper.DistributionType.BIN
  distributionSha256Sum = "0d585f69da091fc5b2beced877feab55a3064d43b8a1d46aeb07996b0915e0e0"
}
