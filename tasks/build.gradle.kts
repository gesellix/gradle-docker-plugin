import java.text.SimpleDateFormat
import java.util.*

plugins {
  id("groovy")
  id("java-library")
  id("maven-publish")
  id("signing")
  id("com.github.ben-manes.versions")
  id("net.ossindex.audit")
  // TODO Validation fails for the java-gradle-plugin "PluginMaven" publication
  // Validation is disabled in the ci/cd workflows (`-x validatePomFileForPluginMavenPublication`)
  id("io.freefair.maven-central.validate-poms")
}

repositories {
  mavenCentral()
}

dependencies {
  api(gradleApi())
  api(localGroovy())

  api("de.gesellix:docker-client:2021-04-10T14-34-47")

  testImplementation("org.spockframework:spock-core:2.0-M5-groovy-3.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.7.1")
  testImplementation("cglib:cglib-nodep:3.3.0")

  // see https://docs.gradle.org/current/userguide/test_kit.html
  testImplementation(gradleTestKit())
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  withType(Test::class.java) {
    useJUnitPlatform()
  }
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("javadoc")
  from(tasks.javadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
}

artifacts {
  add("archives", sourcesJar.get())
  add("archives", javadocJar.get())
}

fun findProperty(s: String) = project.findProperty(s) as String?

val isSnapshot = project.version == "unspecified"
val artifactVersion = if (!isSnapshot) project.version as String else SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date())!!
val publicationName = "gradleDockerPlugin"
publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/${property("github.package-registry.owner")}/${property("github.package-registry.repository")}")
      credentials {
        username = System.getenv("GITHUB_ACTOR") ?: findProperty("github.package-registry.username")
        password = System.getenv("GITHUB_TOKEN") ?: findProperty("github.package-registry.password")
      }
    }
  }
  publications {
    register(publicationName, MavenPublication::class) {
      pom {
        name.set("gradle-docker-plugin")
        description.set("A Docker plugin for Gradle")
        url.set("https://github.com/gesellix/gradle-docker-plugin")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("gesellix")
            name.set("Tobias Gesellchen")
            email.set("tobias@gesellix.de")
          }
        }
        scm {
          connection.set("scm:git:github.com/gesellix/gradle-docker-plugin.git")
          developerConnection.set("scm:git:ssh://github.com/gesellix/gradle-docker-plugin.git")
          url.set("https://github.com/gesellix/gradle-docker-plugin")
        }
      }
      artifactId = "gradle-docker-plugin"
      version = artifactVersion
      from(components["java"])
      artifact(sourcesJar.get())
      artifact(javadocJar.get())
    }
  }
}

signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications[publicationName])
}
