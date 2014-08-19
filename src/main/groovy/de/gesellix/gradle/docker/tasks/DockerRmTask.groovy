package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerRmTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerRmTask)

  @Input
  def containerId

  DockerRmTask() {
    description = "removes a container"
  }

  @TaskAction
  def rm() {
    logger.info "running rm..."
    getDockerClient().rm(getContainerId())
  }
}
