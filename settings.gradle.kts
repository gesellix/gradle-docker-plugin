rootProject.name = "gradle-docker-plugin"
include("plugin")

// https://docs.gradle.org/current/userguide/toolchains.html#sec:provisioning
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
