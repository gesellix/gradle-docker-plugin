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
  api(project(":tasks"))

  testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
  testImplementation("cglib:cglib-nodep:3.3.0")

  // see https://docs.gradle.org/current/userguide/test_kit.html
  testImplementation(gradleTestKit())
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

val isSnapshot = project.version == "unspecified"
var artifactVersion = if (!isSnapshot) project.version as String else SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date())!!
artifactVersion = "local"
project.version = artifactVersion
val publicationName = "gradleDockerPlugin"

val gradle7 = sourceSets.create("gradle7")
java {
  registerFeature(gradle7.name) {
    usingSourceSet(gradle7)
    capability(project.group.toString(), project.name, artifactVersion)
  }
}
configurations.configureEach {
  if (isCanBeConsumed && name.startsWith(gradle7.name)) {
    attributes {
      attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named("7.0"))
    }
  }
}
tasks.named<Copy>(gradle7.processResourcesTaskName) {
  val copyPluginDescriptors = rootSpec.addChild()
  copyPluginDescriptors.into("META-INF/gradle-plugins")
  copyPluginDescriptors.from(tasks.pluginDescriptors)
}
dependencies {
  "gradle7CompileOnly"(gradleApi())
  "gradle7CompileOnly"(localGroovy())
  "gradle7CompileOnly"("de.gesellix:docker-client:2021-04-10T14-34-47")
  "gradle7CompileOnly"(project(":tasks"))
}

tasks {
  withType(Test::class.java) {
    useJUnit()
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

gradlePlugin {
  plugins {
    create(publicationName) {
      implementationClass = "de.gesellix.gradle.docker.DockerPlugin"
      id = "de.gesellix.docker"
    }
  }
}

pluginBundle {
  website = "https://github.com/gesellix/gradle-docker-plugin"
  vcsUrl = "https://github.com/gesellix/gradle-docker-plugin.git"
  description = "A Docker plugin for Gradle"
  tags = listOf("docker", "gradle", "remote api", "plugin")

  (plugins) {
    publicationName {
      displayName = "Gradle Docker plugin"
      version = artifactVersion
    }
  }

  mavenCoordinates {
    groupId = "de.gesellix"
    artifactId = "gradle-docker-plugin"
    version = artifactVersion
  }
}
