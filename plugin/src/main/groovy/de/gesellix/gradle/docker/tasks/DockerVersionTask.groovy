package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerVersionTask extends GenericDockerTask {

  @Internal
  def version

  @Inject
  DockerVersionTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Show the Docker version information"
  }

  @TaskAction
  def version() {
    logger.info "docker version"
    version = getDockerClient().version()
  }
}
