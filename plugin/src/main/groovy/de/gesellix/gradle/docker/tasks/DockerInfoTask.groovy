package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerInfoTask extends GenericDockerTask {

  @Internal
  def info

  DockerInfoTask() {
    description = "Display system-wide information"
    group = "Docker"
  }

  @TaskAction
  def info() {
    logger.info "docker info"
    info = getDockerClient().info()
  }
}
