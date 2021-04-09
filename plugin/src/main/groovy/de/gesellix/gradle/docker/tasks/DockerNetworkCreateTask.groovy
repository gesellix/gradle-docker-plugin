package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerNetworkCreateTask extends GenericDockerTask {

  @Input
  String networkName

  @Input
  def networkConfig = [:]

  @Internal
  def response

  DockerNetworkCreateTask() {
    description = "Create a new network"
    group = "Docker"
  }

  @TaskAction
  def createNetwork() {
    logger.info "docker network create"
    response = getDockerClient().createNetwork(getNetworkName(), getNetworkConfig() ?: [:])
  }
}
