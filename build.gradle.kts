plugins {
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.51.0"
  id("net.ossindex.audit") version "0.4.11"
  id("com.gradle.plugin-publish") version "1.2.2" apply false
  id("io.freefair.maven-central.validate-poms") version "8.10"
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

val groovyVersion = "4.0.15"

val dependencyVersions = listOf(
  "com.squareup.okio:okio:3.9.0",
  "com.squareup.okio:okio-jvm:3.9.0",
  "org.jetbrains:annotations:24.1.0",
  "org.jetbrains.kotlin:kotlin-reflect:1.9.23",
  "org.jetbrains.kotlin:kotlin-stdlib:1.9.23",
  "org.jetbrains.kotlin:kotlin-stdlib-common:1.9.23",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.23",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.23",
)

val dependencyVersionsByGroup = mapOf(
  "org.apache.groovy" to groovyVersion,
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
      dependencySubstitution {
        all {
          requested.let {
            if (it is ModuleComponentSelector && it.group == "org.codehaus.groovy") {
              logger.lifecycle("substituting $it with 'org.apache.groovy:*:${groovyVersion}'")
              useTarget(
                  "org.apache.groovy:${it.module}:${groovyVersion}",
                  "Changed Maven coordinates since Groovy 4"
              )
            }
          }
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
  gradleVersion = "8.4"
  distributionType = Wrapper.DistributionType.BIN
  distributionSha256Sum = "3e1af3ae886920c3ac87f7a91f816c0c7c436f276a6eefdb3da152100fef72ae"
}
