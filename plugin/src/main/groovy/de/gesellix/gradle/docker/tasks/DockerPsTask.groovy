package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerPsTask extends GenericDockerTask {

  @Internal
  def containers

  @Inject
  DockerPsTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "List containers"
  }

  @TaskAction
  def ps() {
    logger.info "docker ps"
    containers = getDockerClient().ps()
  }
}
