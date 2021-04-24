package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerNetworksTask extends GenericDockerTask {

  @Internal
  def networks

  @Inject
  DockerNetworksTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Lists all networks"
  }

  @TaskAction
  def networks() {
    logger.info "docker network ls"
    networks = getDockerClient().networks()
  }
}
