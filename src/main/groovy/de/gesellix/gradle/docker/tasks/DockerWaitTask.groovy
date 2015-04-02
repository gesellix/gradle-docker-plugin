package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerWaitTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerWaitTask)

  @Input
  def containerId

  def result

  DockerWaitTask() {
    description = "Block until a container stops, then print its exit code."
    group = "Docker"
  }

  @TaskAction
  def stop() {
    logger.info "docker wait"
    result = getDockerClient().wait(getContainerId())
    return result
  }
}
