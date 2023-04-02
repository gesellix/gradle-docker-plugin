rootProject.name = "gradle-docker-plugin"
include("plugin")

// https://docs.gradle.org/8.0.1/userguide/toolchains.html#sub:download_repositories
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}
