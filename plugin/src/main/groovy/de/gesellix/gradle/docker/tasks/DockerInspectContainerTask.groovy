package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerInspectContainerTask extends GenericDockerTask {

  @Input
  def containerId

  @Internal
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
