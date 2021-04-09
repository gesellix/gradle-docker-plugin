package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerServiceCreateTask extends GenericDockerTask {

  @Input
  def serviceConfig = [:]

  @Internal
  def response

  DockerServiceCreateTask() {
    description = "Create a service"
    group = "Docker"
  }

  @TaskAction
  def createService() {
    logger.info "docker service create"
    response = getDockerClient().createService(getServiceConfig() ?: [:])
  }
}
