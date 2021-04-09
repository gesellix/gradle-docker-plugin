package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerServiceRmTask extends GenericDockerTask {

  @Input
  def serviceName = [:]

  @Internal
  def response

  DockerServiceRmTask() {
    description = "Remove a service"
    group = "Docker"
  }

  @TaskAction
  def rmService() {
    logger.info "docker service rm"
    response = getDockerClient().rmService(getServiceName())
  }
}
