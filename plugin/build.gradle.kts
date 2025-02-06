import io.freefair.gradle.plugins.maven.central.ValidateMavenPom
import java.text.SimpleDateFormat
import java.util.*

plugins {
  id("groovy")
  id("java-gradle-plugin")
  id("maven-publish")
  id("signing")
  id("com.github.ben-manes.versions")
  id("net.ossindex.audit")
  id("com.gradle.plugin-publish")
  id("io.freefair.maven-central.validate-poms")
}

repositories {
//  mavenLocal()
//  fun findProperty(s: String) = project.findProperty(s) as String?
//  listOf(
//    "docker-client/*",
//    "gesellix/*"
//  ).forEach { repo ->
//    maven {
//      name = "github"
//      setUrl("https://maven.pkg.github.com/$repo")
//      credentials {
//        username = System.getenv("PACKAGE_REGISTRY_USER") ?: findProperty("github.package-registry.username")
//        password = System.getenv("PACKAGE_REGISTRY_TOKEN") ?: findProperty("github.package-registry.password")
//      }
//    }
//  }
  mavenCentral()
}

dependencies {
  api(gradleApi())

  api("de.gesellix:docker-client:2025-01-19T00-00-00")

  testImplementation(localGroovy())
  testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
  testImplementation("cglib:cglib-nodep:3.3.0")

  // see https://docs.gradle.org/current/userguide/test_kit.html
  testImplementation(gradleTestKit())
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
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

val localRepositoryName = "LocalPackages"
val gitHubPackagesRepositoryName = "GitHubPackages"
val isSnapshot = project.version == "unspecified"
val artifactVersion = if (!isSnapshot) project.version as String else SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date())!!
val publicationName = "gradleDockerPlugin"
publishing {
  repositories {
    maven {
      name = localRepositoryName
      url = uri("../local-plugins")
    }
    maven {
      name = gitHubPackagesRepositoryName
      url = uri("https://maven.pkg.github.com/${property("github.package-registry.owner")}/${property("github.package-registry.repository")}")
      credentials {
        username = System.getenv("GITHUB_ACTOR") ?: findProperty("github.package-registry.username")
        password = System.getenv("GITHUB_TOKEN") ?: findProperty("github.package-registry.password")
      }
    }
  }
  publications {
    register<MavenPublication>(publicationName) {
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
      // TODO how do we ensure that these artifacts will always be added
      // automatically?
//      artifact(sourcesJar.get())
//      artifact(javadocJar.get())
    }
  }
}

signing {
  setRequired({ !isSnapshot })
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications[publicationName])
}

gradlePlugin {
  website.set("https://github.com/gesellix/gradle-docker-plugin")
  vcsUrl.set("https://github.com/gesellix/gradle-docker-plugin.git")

  plugins {
    register(publicationName) {
      id = "de.gesellix.docker"
      displayName = "Gradle Docker plugin"
      description = "A Docker plugin for Gradle"
      implementationClass = "de.gesellix.gradle.docker.DockerPlugin"
      version = artifactVersion
      tags.set(listOf("docker", "remote api", "client"))
    }
  }
}

tasks.withType<ValidateMavenPom>().configureEach {
  ignoreFailures = System.getenv()["IGNORE_INVALID_POMS"] == "true"
      || name.contains("For${publicationName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}PluginMarkerMaven")
      || name.contains("ForPluginMavenPublication")
}

tasks.register("publishTo${localRepositoryName}") {
  group = "publishing"
  description = "Publishes all Maven publications to the $localRepositoryName Maven repository."
  dependsOn(tasks.withType<PublishToMavenRepository>().matching {
    it.repository == publishing.repositories[localRepositoryName]
  })
}

tasks.register("publishTo${gitHubPackagesRepositoryName}") {
  group = "publishing"
  description = "Publishes all Maven publications to the $gitHubPackagesRepositoryName Maven repository."
  dependsOn(tasks.withType<PublishToMavenRepository>().matching {
    it.repository == publishing.repositories[gitHubPackagesRepositoryName]
  })
}

val isLocalRepo = { repository: MavenArtifactRepository ->
  repository == publishing.repositories[localRepositoryName]
}
val isStandardMavenPublication = { repository: MavenArtifactRepository, publication: MavenPublication ->
  publication == publishing.publications[publicationName]
      && repository.name in listOf("sonatype", localRepositoryName, gitHubPackagesRepositoryName)
}
val isGradlePluginPublish = { repository: MavenArtifactRepository, publication: MavenPublication ->
  publication == publishing.publications["pluginMaven"]
      && repository.name !in listOf("sonatype", localRepositoryName, gitHubPackagesRepositoryName)
}

tasks.withType<PublishToMavenRepository>().configureEach {
  onlyIf {
    isLocalRepo(repository)
        || isStandardMavenPublication(repository, publication)
        || isGradlePluginPublish(repository, publication)
  }
  mustRunAfter(tasks.withType<Sign>())
}

//afterEvaluate {
//  publishing.publications.forEach { p ->
//    if (p is MavenPublication){
//      p.artifacts.forEach {a->
//        println("${p.name} -> ${a.extension}/${a.classifier} -> ${a.file}")
//      }
//    }
//  }
//}
