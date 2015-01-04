package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerImagesTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerPsTask)

  def images

  DockerImagesTask() {
    description = "List images"
    group = "Docker"
  }

  @TaskAction
  def images() {
    logger.info "docker images"
    images = getDockerClient().images()
  }
}
