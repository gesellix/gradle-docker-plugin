package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerRmiTask extends DockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerRmiTask)

  @Input
  def imageId

  DockerRmiTask() {
    description = "Remove one or more images"
    group = "Docker"
  }

  @TaskAction
  def rmi() {
    logger.info "docker rmi"
    getDockerClient().rmi(getImageId())
  }
}
