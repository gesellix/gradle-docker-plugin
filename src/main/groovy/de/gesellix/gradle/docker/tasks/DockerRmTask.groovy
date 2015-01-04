package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerRmTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerRmTask)

  @Input
  def containerId

  def result

  DockerRmTask() {
    description = "Remove one or more containers"
    group = "Docker"
  }

  @TaskAction
  def rm() {
    logger.info "docker rm"
    result = getDockerClient().rm(getContainerId())
    return result
  }
}
