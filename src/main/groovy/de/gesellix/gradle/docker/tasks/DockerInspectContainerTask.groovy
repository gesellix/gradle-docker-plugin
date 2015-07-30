package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerInspectContainerTask extends DockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerInspectContainerTask)

  @Input
  def containerId

  def containerInfo

  DockerInspectContainerTask() {
    description = "Return low-level information on a container"
    group = "Docker"
  }

  @TaskAction
  def inspect() {
    logger.info "docker inspect"

    containerInfo = getDockerClient().inspectContainer(getContainerId())
  }
}
