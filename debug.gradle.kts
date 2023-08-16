import de.gesellix.docker.client.DockerClientImpl

buildscript {
  repositories {
//    mavenLocal()
//    fun findProperty(s: String) = project.findProperty(s) as String?
//    listOf(
//      "docker-client/*",
//      "gesellix/*"
//    ).forEach { repo ->
//      maven {
//        name = "github"
//        setUrl("https://maven.pkg.github.com/$repo")
//        credentials {
//          username = System.getenv("PACKAGE_REGISTRY_USER") ?: findProperty("github.package-registry.username")
//          password = System.getenv("PACKAGE_REGISTRY_TOKEN") ?: findProperty("github.package-registry.password")
//        }
//      }
//    }
    mavenCentral()
  }

  dependencies {
    classpath("de.gesellix:docker-client:2023-08-16T08-25-00")
  }
}

tasks.register("checkDockerAvailability") {
  group = "docker"
  val client = DockerClientImpl()

  doFirst {
    logger.lifecycle("Docker Host:\n|> ${client.env.dockerHost} <|")
  }
  doLast {
    try {
      logger.lifecycle("Docker Ping:\n|> ${client.ping().content} <|")
      logger.lifecycle("Docker Version:\n|> ${client.version().content} <|")
      logger.lifecycle("Docker Info:\n|> ${client.info().content} <|")
    } catch (e: Exception) {
      logger.warn("Docker Engine not available?", e)
    }
  }
}
