package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerVersionTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerVersionTask)

  def version

  DockerVersionTask() {
    description = "get the docker version information"
    group = "Docker"
  }

  @TaskAction
  def version() {
    logger.info "running version..."
    version = getDockerClient().version()
  }
}
