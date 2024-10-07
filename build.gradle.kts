plugins {
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.51.0"
  id("net.ossindex.audit") version "0.4.11"
  id("com.gradle.plugin-publish") version "1.3.0" apply false
  id("io.freefair.maven-central.validate-poms") version "8.10.2"
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

val dependencyVersions = listOf(
  "com.squareup.okio:okio:3.9.1",
  "com.squareup.okio:okio-jvm:3.9.1",
  "org.jetbrains:annotations:26.0.1",
  "org.jetbrains.kotlin:kotlin-reflect:1.9.24",
  "org.jetbrains.kotlin:kotlin-stdlib:1.9.24",
  "org.jetbrains.kotlin:kotlin-stdlib-common:1.9.24",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.24",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.24",
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

// https://gradle.org/release-checksums/
tasks.wrapper {
  gradleVersion = "8.10.1"
  distributionType = Wrapper.DistributionType.BIN
  distributionSha256Sum = "1541fa36599e12857140465f3c91a97409b4512501c26f9631fb113e392c5bd1"
}
