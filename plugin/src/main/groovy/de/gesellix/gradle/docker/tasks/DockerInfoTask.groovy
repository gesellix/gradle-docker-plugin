package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerInfoTask extends GenericDockerTask {

  @Internal
  def info

  @Inject
  DockerInfoTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Display system-wide information"
  }

  @TaskAction
  def info() {
    logger.info "docker info"
    info = getDockerClient().info()
  }
}
