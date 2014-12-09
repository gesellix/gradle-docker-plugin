package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerStartTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerStopTask)

  @Input
  def containerId

  DockerStartTask() {
    description = "starts a container"
    group = "Docker"
  }

  @TaskAction
  def start() {
    logger.info "running start..."
    getDockerClient().start(getContainerId())
  }
}
