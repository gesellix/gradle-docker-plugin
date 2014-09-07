package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerStopTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerStopTask)

  @Input
  def containerId

  DockerStopTask() {
    description = "stops a container"
    group = "Docker"
  }

  @TaskAction
  def stop() {
    logger.info "running stop..."
    getDockerClient().stop(getContainerId())
  }
}
